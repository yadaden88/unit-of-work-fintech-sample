package org.example;

public interface Repository<T> {

    T save(T entity);

    void update(T entity);
}

