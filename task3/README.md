# Task 3 – Reactive Rakuten Books (R2DBC / H2)

Spring Boot WebFlux + R2DBC H2. On startup, `RakutenBooksForeignBookSetup` calls the Rakuten **BooksForeignBook/Search/20170404** API, parses JSON with a reactive pipeline (`JsonNode` → `Flux`), and saves rows via `ForeignBookRepository`.

## Configure credentials (required)

```powershell
$env:RAKUTEN_APPLICATION_ID = "<your-application-id>"
$env:RAKUTEN_ACCESS_KEY = "<your-access-key>"
mvn spring-boot:run
```

## Request (matches your curl)

- **Base URL:** `https://openapi.rakuten.co.jp`
- **Path:** `/services/api/BooksForeignBook/Search/20170404`
- **Headers:** `Referer: https://example.com/java-adv-be-services-task3`
- **Query:** `format`, `title`, `booksGenreId`, `applicationId`, `accessKey`

## H2 / R2DBC

In-memory H2 with PostgreSQL compatibility mode. Schema: `src/main/resources/schema.sql`, applied via `ConnectionFactoryInitializer`.

### H2 Console (while the app is running)

1. Start the app.
2. Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
3. JDBC URL: `jdbc:h2:mem:task3;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`
4. User `sa`, password empty → **Connect**.
5. Run `SELECT * FROM foreign_book;`
