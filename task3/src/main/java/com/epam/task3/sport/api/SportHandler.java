package com.epam.task3.sport.api;

import com.epam.task3.sport.Sport;
import com.epam.task3.sport.SportAlreadyExistsException;
import com.epam.task3.sport.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class SportHandler {

    private final SportService sportService;

    public Mono<ServerResponse> createSport(ServerRequest request) {
        String sportName = request.pathVariable("sportname");
        return sportService
                .create(sportName)
                .flatMap(this::createdResponse)
                .onErrorResume(SportAlreadyExistsException.class, ex -> conflict(ex.getMessage()))
                .onErrorResume(IllegalArgumentException.class, ex -> badRequest(ex.getMessage()));
    }

    public Mono<ServerResponse> searchSports(ServerRequest request) {
        String q = request.queryParam("q").orElse("");
        return sportService
                .searchByName(q)
                .collectList()
                .flatMap(sports -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(sports));
    }

    private Mono<ServerResponse> createdResponse(Sport sport) {
        URI location = URI.create("/api/v1/sport/" + sport.getName());
        return ServerResponse.created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sport);
    }

    private Mono<ServerResponse> conflict(String message) {
        return ServerResponse.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(message));
    }

    private Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(message));
    }
}
