package com.epam.task3.sport;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SportRepository extends ReactiveCrudRepository<Sport, Integer> {

    Mono<Boolean> existsByName(String name);

    Mono<Sport> findByName(String name);

    Flux<Sport> findByNameContainingIgnoreCase(String namePart);
}
