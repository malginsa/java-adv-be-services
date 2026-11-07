package com.epam.task3.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient rakutenOpenApiWebClient(RakutenBooksProperties properties) {
        // Use JDK InetAddress resolution (same as ping/curl); Netty's async DNS can fail on some Windows setups.
        HttpClient httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                                .build())
                .build();
    }
}
