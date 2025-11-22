package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;
    private final TransferRepository transferRepository;

    public TransferController(TransferService transferService, TransferRepository transferRepository) {
        this.transferService = transferService;
        this.transferRepository = transferRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer createTransfer(@RequestBody CreateTransferRequest request) {
        return transferService.createTransfer(
            request.fromAccountId(),
            request.toAccountId(),
            request.amount()
        );
    }

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public record CreateTransferRequest(UUID fromAccountId, UUID toAccountId, long amount) {
    }
}

