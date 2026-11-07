package com.epam.task3.setup;

import com.epam.task3.book.ForeignBook;
import com.epam.task3.book.ForeignBookRepository;
import com.epam.task3.config.RakutenBooksProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * On startup: calls Rakuten BooksForeignBook Search API, maps JSON to entities, persists via R2DBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RakutenBooksForeignBookSetup implements ApplicationRunner {

    private final WebClient rakutenOpenApiWebClient;
    private final RakutenBooksProperties properties;
    private final ForeignBookRepository foreignBookRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getApplicationId()) || !StringUtils.hasText(properties.getAccessKey())) {
            log.warn(
                    "Skip Rakuten import: set RAKUTEN_APPLICATION_ID and RAKUTEN_ACCESS_KEY (or configure rakuten.books-foreign-book.application-id / access-key).");
            return;
        }

        fetchAndPersist()
                .doOnSuccess(v -> log.info("Rakuten BooksForeignBook import finished"))
                .doOnError(ex -> log.error("Rakuten BooksForeignBook import failed", ex))
                .subscribe();
    }

    Mono<Void> fetchAndPersist() {
        return rakutenOpenApiWebClient
                .get()
                .uri(this::buildSearchUri)
                .header(HttpHeaders.REFERER, properties.getReferer())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(this::itemsFlux)
                .mapNotNull(this::toEntity)
                .filter(b -> StringUtils.hasText(b.getTitle()))
                .as(foreignBookRepository::saveAll)
                .then();
    }

    private java.net.URI buildSearchUri(UriBuilder builder) {
        return builder.path(properties.getApiVersionPath())
                .queryParam("format", properties.getFormat())
                .queryParam("title", properties.getTitle())
                .queryParam("booksGenreId", properties.getBooksGenreId())
                .queryParam("applicationId", properties.getApplicationId())
                .queryParam("accessKey", properties.getAccessKey())
                .build();
    }

    /**
     * Rakuten wraps each hit as {@code Items[].Item}.
     */
    private Flux<JsonNode> itemsFlux(JsonNode root) {
        JsonNode items = root.path("Items");
        if (!items.isArray()) {
            return Flux.empty();
        }
        return Flux.fromStream(StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(items.elements(), Spliterator.ORDERED), false)
                .map(node -> node.path("Item"))
                .filter(item -> !item.isMissingNode() && !item.isNull()));
    }

    private ForeignBook toEntity(JsonNode item) {
        return ForeignBook.builder()
                .isbn(firstNonBlank(item, "isbn", "ISBN", "isbnjan", "isbnJan"))
                .title(textOrNull(item, "title"))
                .author(textOrNull(item, "author"))
                .publisherName(textOrNull(item, "publisherName"))
                .itemPrice(intOrNull(item, "itemPrice"))
                .itemUrl(textOrNull(item, "itemUrl"))
                .build();
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode child = node.path(field);
        if (child.isMissingNode() || child.isNull()) {
            return null;
        }
        String s = child.asText();
        return StringUtils.hasText(s) ? s : null;
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode child = node.path(field);
        if (child.isMissingNode() || child.isNull() || !child.canConvertToInt()) {
            return null;
        }
        return child.asInt();
    }

    private static String firstNonBlank(JsonNode node, String... names) {
        for (String name : names) {
            String v = textOrNull(node, name);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }
}
