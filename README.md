# Unit of Work with Optimistic Locking for Fintech Applications

## 📖 Project Overview

This project demonstrates a high-performance architectural pattern designed for fintech applications where data consistency and high concurrency are critical. It solves a specific problem inherent in financial software: **how to apply complex business logic to a ledger without holding database locks for extended periods.**

### The Core Problem

In traditional transaction management, a database transaction is often kept open while the application performs calculations, validations, or external API calls. This leads to:

* **Row Locking:** Database rows are locked for the duration of the entire operation.
* **Reduced Concurrency:** Other threads cannot touch those accounts until the long process finishes.
* **Deadlocks:** Increased risk of database deadlocks under high load.

### The Solution

This implementation uses a [Unit of Work](https://martinfowler.com/eaaCatalog/unitOfWork.html) pattern combined with [Optimistic Locking](https://martinfowler.com/eaaCatalog/optimisticOfflineLock.html). Database interactions are deferred until the final moment (commit time), keeping the "lock window" extremely small - typically just a few milliseconds - regardless of how complex the business logic is.

-----

## 🏗 Pattern Explanation

### 1\. The Unit of Work (Staging)

Instead of writing to the database immediately, business logic interacts with a `Batch` object.

* **Accumulation:** You register entities via `batch.insert(entity)` or `batch.update(entity)`.
* **Deferred Execution:** These methods are purely in-memory operations. No SQL is executed during the business logic phase.

### 2\. Optimistic Locking

Entities (like `Account`) carry a `version` number.

* Updates are performed with a guard clause: `UPDATE ... WHERE id = ? AND version = ?`.
* If the version in the database does not match the version held in memory, the update affects 0 rows.
* The repository detects this and throws an `OptimisticLockException`.

### 3\. Automatic Retry Loop

The `UnitOfWork` wraps the business logic in a retry mechanism:

1.  It executes the logic.
2.  If an `OptimisticLockException` occurs during commit, it catches the exception.
3.  It **discards the stale batch** and **re-executes the logic lambda**.
4.  This forces the logic to re-fetch fresh data from the database and recalculate.

-----

## 🚀 Key Benefits

* **High Concurrency:** By avoiding long-running transactions, thousands of concurrent requests can prepare data simultaneously without blocking each other.
* **Deadlock Prevention:** The `Batch` class automatically sorts all update operations by Entity ID before execution. This guarantees a consistent lock acquisition order.
* **Safe External Calls:** You can safely make HTTP calls (e.g., to payment gateways) inside your business logic because the database transaction has not started yet.
* **Developer Experience:** The pattern abstracts complexity. Developers work with standard POJOs, and the infrastructure handles the transaction lifecycle.

-----

## ⚠️ Important Trade-offs & Requirements

### The Idempotency Requirement

**Crucial:** Because the `UnitOfWork` may execute your business logic multiple times (in case of version conflicts), **your business logic block must be idempotent.**

* **Do not** perform non-reversible side effects (like sending an email or charging a credit card) directly inside the retriable block unless you have a mechanism to handle duplicates.
* **Best Practice:** Keep the retriable block focused on data fetching and calculation.

### Freshness Constraint

You must always fetch data **inside** the lambda passed to `executeRetriable`. Passing entities in from the outside will cause infinite loops because the retries will keep using the stale, older version of the entity.

-----

## 🛠 Technical Implementation Details

### Components

* **`UnitOfWork.java`**: The orchestrator. It manages the `TransactionTemplate`, handles the retry loop (default max 10 retries), and triggers the final commit.
* **`Batch.java`**: The staging area. It holds lists of entities to insert or update. It also handles the sorting of updates to prevent deadlocks.
* **`RepositoryConfig.java`**: Acts as a registry, mapping Entity classes to their specific Repositories so the `Batch` knows where to save them.
* **`AccountRepository.java`**: Implements the optimistic locking check. It throws an exception if `rowsUpdated == 0`.

### Technology Stack

* **Java 21+** (uses Records)
* **Spring Boot** (Transaction management)
* **jOOQ** (Type-safe SQL execution)
* **Liquibase** (Schema management)
* **Testcontainers** (Integration testing with real DB)

-----

## 🧪 Testing & Demonstration

The project includes a robust test suite designed to verify both functional correctness and thread safety under extreme load.

### 1\. Functional Integration Tests (`ApplicationTest.java`)

These tests verify the end-to-end flow using the REST API:

* **Account Lifecycle:** verifies that accounts can be created with random balances and correctly persisted with generated IDs.
* **Atomic Transfers:** verifies the "Happy Path" of a transfer. It checks that:
    * Funds are deducted from the source account.
    * Funds are added to the destination account.
    * A persistent `Transfer` record is created with the correct metadata.

### 2\. High-Concurrency Stress Test (`ConcurrentTransferTest.java`)

This is the critical proof-of-concept test. It simulates a high-frequency trading environment to ensure the Optimistic Locking mechanism prevents "Lost Updates."

* **The Scenario:**
    * **50 Threads** running simultaneously.
    * **100 Accounts** created.
    * **10,000 Transfers** executed aggressively between random accounts.
* **The Assertion (Conservation of Money):**
    * The test calculates the *Total System Balance* (sum of all accounts) before execution.
    * It waits for all 10,000 transfers to finish.
    * It calculates the *Total System Balance* again.
    * **Success Criteria:** The total balance must remain exactly the same.

**Why this is important:**
If the Unit of Work pattern failed (e.g., if two threads read the same balance, modified it, and wrote it back without version checks), one of those updates would overwrite the other, resulting in money disappearing from the system. This test proves that despite thousands of collisions, the retry mechanism eventually resolves every transaction consistently.

-----

## 💻 Usage Example

Here is how to implement a transfer service using this pattern:

```java
@Service
public class TransferService {

    // ... constructor injection ...

    public Transfer createTransfer(UUID fromId, UUID toId, long amount) {
        // Start the retriable unit of work
        return new UnitOfWork(transactionTemplate).executeRetriable(batch -> {
            
            // 1. FETCH: Always fetch fresh data inside the lambda
            var fromAccount = accountRepository.findById(fromId);
            var toAccount = accountRepository.findById(toId);

            // 2. LOGIC: Calculate new state (Pure Java)
            var updatedFrom = fromAccount.withBalance(fromAccount.balance() - amount);
            var updatedTo = toAccount.withBalance(toAccount.balance() + amount);
            
            var transfer = new Transfer(UUID.randomUUID(), fromId, toId, amount);

            // 3. REGISTER: Stage the changes (No DB write yet)
            batch.update(updatedFrom);
            batch.update(updatedTo);
            batch.insert(transfer);

            // 4. RETURN: The result object
            return transfer;
        }); 
        // Commit happens automatically here. 
        // If conflict occurs, the lambda runs again.
    }
}
```