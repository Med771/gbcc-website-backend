# Покрытие тестами: приоритеты и пробелы

Отчёт JaCoCo: после `mvn verify` открыть `target/site/jacoco/index.html`.

## Высокий приоритет (бизнес + безопасность)

| Область | Что уже есть | Что усилить |
|---------|----------------|-------------|
| `logic/order` | Матрица переходов статусов и негативы в `OrderServiceImplTest` / `OrderControllerWebMvcTest`, E2E `OrderLifecycleIntegrationTest` (Docker) | Граничные кейсы позиций, неактивный товар, доп. WebMvc по edge-кейсам |
| `logic/account` | `AccountServiceImplTest`, `AccountControllerWebMvcTest` | Конфликты email/phone, права ADMIN при поиске |
| `logic/auth` | `AuthServiceImplTest`, `AuthControllerWebMvcTest` | Refresh rotation, logout, blocked account |
| `filter`, `helper` | `JwtAuthenticationFilterTest`, `JwtTokenHelperTest`, `AuthCookieHelperTest`, `SecurityContextHelperTest` | Невалидный JWT, истёкший токен (если появится проверка) |
| `logic/promotion` | `PromotionServiceImplTest`, `PromotionDiscountResolverTest`, `PromotionControllerWebMvcTest` | `patch`, `search` с фильтрами, видимость для не-админа |

## Средний приоритет

| Область | Примечание |
|---------|------------|
| MapStruct (`*Mapper`) | Тонкие тесты полей или опора на интеграционные сценарии |
| JPA `*Specification` | При сложных Criteria — интеграция на Testcontainers |
| `config`, `tool` | Точечные unit-тесты при изменении логики |

## Низкий приоритет / исключения из жёсткого gate

- Сгенерированные DTO/records (часто исключаются из JaCoCo `check`).
- Тонкие entity без логики.

## Интеграция (Docker)

Классы с `@Tag("requires-docker")` и Testcontainers не входят в обычный `mvn test`.

- Полный прогон с контекстом и БД: `mvn verify -Pintegration-tests` (нужен Docker).

См. также [README.md](../README.md) раздел «Тесты».
