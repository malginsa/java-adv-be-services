package com.epam.task3.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "rakuten.books-foreign-book")
public class RakutenBooksProperties {

    @NotBlank
    private String baseUrl = "https://openapi.rakuten.co.jp";

    @NotBlank
    private String apiVersionPath = "/services/api/BooksForeignBook/Search/20170404";

    @NotBlank
    private String referer = "https://example.com/java-adv-be-services-task3";

    @NotBlank
    private String title = "Potter";

    @NotBlank
    private String booksGenreId = "005407";

    @NotBlank
    private String format = "json";

    /**
     * Set via {@code RAKUTEN_APPLICATION_ID} or {@code application.yml}. Never commit real keys.
     */
    private String applicationId = "";

    private String accessKey = "";
}
