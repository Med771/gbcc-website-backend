# Покрытие тестами: приоритеты и пробелы

Отчёт JaCoCo: после `mvn verify` открыть `target/site/jacoco/index.html`.

## Закрытые «дыры» (2026-05-13)

Ниже — 10 пробелов, которые были закрыты добавленными тестами и общими фикстурами примеров для IT.

| № | Была дыра | Что сделано |
|---|-----------|-------------|
| 1 | Нет unit-тестов на **блокировку аккаунта** при логине | `AuthServiceImplTest.login_shouldThrowForbidden_whenAccountBlocked` |
| 2 | Нет негативов **refresh** (пустой токен, неизвестный hash, отозван/истёк, заблокированный аккаунт) | `AuthServiceImplTest` — серия методов `refresh_shouldThrow*` |
| 3 | **Logout** не проверял отзыв refresh и no-op при пустом значении | `AuthServiceImplTest.logout_*` |
| 4 | **SHA-256** refresh-хеша без регрессионных векторов | `TokenHashHelperTest` |
| 5 | **409 при дубликате** email/телефона только в IT, не в unit слое аккаунтов | `AccountServiceImplTest` — `DuplicateKeyException` → `CONFLICT` |
| 6 | Заказ с **неактивным товаром** без явного unit | `OrderServiceImplTest.create_shouldThrowBadRequest_whenProductInactive` |
| 7 | **JWT filter**: только валидный токен и blocked; нет «мусорного» JWT | `JwtAuthenticationFilterTest.doFilterInternal_shouldNotAuthenticate_whenJwtMalformed` |
| 8 | **Поиск акций** с query-параметрами не проверялся на границе контроллера | `PromotionControllerWebMvcTest.search_public_passesFilterParamsToService` |
| 9 | **patch / search** в `PromotionServiceImpl` без позитивных unit | `PromotionServiceImplTest.patch_shouldUpdateName_whenAdmin`, `search_shouldApplyGuestVisibility_andMapRows` |
| 10 | Дублирование JSON в IT и нет **публичных read-сценариев** на типовых URL | `IntegrationExampleRequests`, рефакторинг `CustomerAuthFlowIntegrationTest`, `PublicReadEndpointsIntegrationTest` (`requires-external-db`) |

Примеры тел запросов для интеграции: `src/test/java/backend/website/gbcc/integration/fixtures/IntegrationExampleRequests.java`.

---

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
| JPA `*Specification` | При сложных Criteria — интеграция на реальной БД (профиль `integration-external`) |
| `config`, `tool` | Точечные unit-тесты при изменении логики |

## Низкий приоритет / исключения из жёсткого gate

- Сгенерированные DTO/records (часто исключаются из JaCoCo `check`).
- Тонкие entity без логики.

## Интеграция (внешняя PostgreSQL)

Интеграционные классы не входят в обычный `mvn test` (Surefire: `excludes` для `GbccWebsiteBackendApplicationTests` и `**/integration/*IntegrationTest.java`). Тег `requires-external-db` остаётся для фильтрации вручную в IDE. Нужна PostgreSQL с Flyway (см. `application-integration-external.yaml`, по умолчанию порт **5555** как в compose). Полный прогон: `mvn verify -Pintegration-tests` или `-Pintegration-external`.

См. также [README.md](../README.md) раздел «Тесты».
