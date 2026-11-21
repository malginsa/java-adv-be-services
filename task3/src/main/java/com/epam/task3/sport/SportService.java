package com.epam.task3.sport;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sportRepository;

    public Mono<Sport> create(String rawName) {
        String name = normalizeName(rawName);
        if (name == null) {
            return Mono.error(new IllegalArgumentException("Sport name must not be blank"));
        }

        return sportRepository
                .existsByName(name)
                .flatMap(exists -> exists ? Mono.error(new SportAlreadyExistsException(name)) : Mono.just(name))
                .map(n -> Sport.builder().name(n).build())
                .flatMap(sportRepository::save)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new SportAlreadyExistsException(name));
    }

    public Flux<Sport> searchByName(String query) {
        if (!StringUtils.hasText(query)) {
            return sportRepository.findAll();
        }
        return sportRepository.findByNameContainingIgnoreCase(query.trim());
    }

    private static String normalizeName(String rawName) {
        if (rawName == null) {
            return null;
        }
        String trimmed = rawName.trim();
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }
}
