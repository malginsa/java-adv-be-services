package com.epam.task3.sport.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class SportRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> sportRoutes(SportHandler handler) {
        return RouterFunctions.route(
                        POST("/api/v1/sport/{sportname}"), handler::createSport)
                .andRoute(GET("/api/v1/sport"), handler::searchSports);
    }
}
