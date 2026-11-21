package com.epam.task3.book;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ForeignBookRepository extends ReactiveCrudRepository<ForeignBook, Long> {}
