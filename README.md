# GBCC Website Backend

Backend API for product catalog and file storage.

## Tech Stack

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Flyway
- PostgreSQL
- MapStruct
- Lombok
- springdoc OpenAPI (Swagger UI)

## Project Structure

- `src/main/java/backend/website/gbcc/config` - framework and app configuration
- `src/main/java/backend/website/gbcc/handler` - global exception handling
- `src/main/java/backend/website/gbcc/logic/product` - product domain (controller/service/repository/specification)
- `src/main/java/backend/website/gbcc/logic/file` - file domain (upload/download/delete)
- `src/main/java/backend/website/gbcc/model` - shared entities and dto/error models
- `src/main/java/backend/website/gbcc/tool` - taxonomy helper tools (`getOrCreate`)
- `src/main/resources/db/migration` - Flyway SQL migrations
- `src/test/java` - tests

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL 15+ (or compatible)

## Local Run

1. Configure database and storage path in `src/main/resources/application.yaml`.
2. Start PostgreSQL and create database.
3. Run app:

```bash
mvn spring-boot:run
```

Or build and run jar:

```bash
mvn clean package
java -jar target/gbcc-website-backend-0.0.1-SNAPSHOT.jar
```

## Migrations

Flyway migrations are executed on startup from:

- `classpath:db/migration`

Naming pattern:

- `V###__description.sql`

## API Overview

### Product

- `POST /product` - create product
- `GET /product` - search products with filters and pagination
- `POST /product/{productId}/photos` - attach photo to product

### File

- `POST /file` (`multipart/form-data`) - upload file
- `GET /file/{key}` - download file by key
- `DELETE /file/{key}` - delete file by key

## Validation and Error Contract

- Input validation uses Jakarta Validation annotations (`@Valid`, `@Validated`).
- Business validations are done in services where cross-field checks are needed.
- All errors are normalized by `GlobalExceptionHandler` into `ApiErrorResponse`.

## Testing

Run all tests:

```bash
mvn test
```

Run compile only:

```bash
mvn -DskipTests compile
```

Current direction:

- controller slice tests (`@WebMvcTest`) for validation and error contract
- service unit tests for business rules
- next step: integration tests for persistence and migration-sensitive scenarios
