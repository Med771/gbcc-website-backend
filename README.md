# GBCC Website Backend

REST API для сайта GBCC: каталог товаров, файлы, новости, акции (скидки), заказы, учётные записи с JWT в cookies, обращения в поддержку (в т.ч. от гостей), заявки с формы обратной связи («Свяжитесь с нами»), **модуль CRM** (`/crm/**`: организации, лиды, взаимодействия, задачи, B2B-поставки и контракты, карта, метрики).

Полная интерактивная документация: **Swagger UI** — после запуска приложения откройте в браузере:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Технологии

| Компонент | Версия / заметка |
|-----------|------------------|
| Java | 21 |
| Spring Boot | 4.x |
| Spring Web MVC, Security, Data JPA | |
| PostgreSQL | 15+ |
| Flyway | миграции схемы |
| MapStruct, Lombok | |
| springdoc-openapi | Swagger UI |

---

## Структура проекта

- `config` — Security, JWT, Swagger/OpenAPI, JPA auditing, инициализация owner-аккаунта
- `filter` — JWT из cookie `GBCC_ACCESS_TOKEN`
- `handler` — `GlobalExceptionHandler`, единый формат `ApiErrorResponse`
- `logic/*` — домены: `auth`, `account`, `product`, `file`, `news`, `promotion`, `order`, `support`, `contactrequest`, **`analytics`** (публичный сбор событий сайта), **`crm`** (организации, лиды, interactions, tasks, supplies, contracts, map, metrics, **аналитика сайта**)
- `model` — общие enum, DTO ошибок, базовые сущности
- `resources/db/migration` — SQL миграции Flyway (`V###__description.sql`)
- `test` — unit-тесты (Mockito), WebMvc-срезы (`@GbccWebMvcTest`), интеграционные тесты против **внешней** PostgreSQL (профили `test` + `integration-external`, тег `requires-external-db` для навигации). Обычный `mvn test` **не поднимает** их: Surefire исключает `GbccWebsiteBackendApplicationTests` и `**/integration/*IntegrationTest.java`. Полный прогон с IT: поднять БД (`docker-compose.yaml` в корне), затем `mvn verify -Pintegration-tests` или `-Pintegration-external`.
- `docs/TZ_CRM_BACKEND_ALIGNMENT.md` — сверка ТЗ CRM с реализацией
- `docs/EXPERT_GUIDE.md` — единый экспертный обзор (роли, B2C/B2B, безопасность, тесты, ссылки на остальные документы)

---

## CRM API (`/crm/**`)

Доступ: **JWT** (роли **ADMIN** или **OWNER**). В Swagger — схема `accessTokenCookie`. **CUSTOMER** получает 403 на бизнес-операциях CRM.

- **Организации:** `GET/POST/PUT /crm/organizations`, `PATCH /crm/organizations/{id}/assign` (только OWNER), контакты под `/crm/organizations/{id}/contacts`, история `.../contacts/{contactId}/history`.
- **Взаимодействия:** `/crm/interactions` (ровно одно из `organizationId` или `leadId` в теле).
- **Задачи:** `/crm/tasks`, `GET /crm/tasks/overdue`.
- **Лиды:** `/crm/leads`, `POST /crm/leads/{id}/convert` → создаётся организация.
- **Поставки B2B:** `/crm/supplies` (отдельно от заказов витрины `/order`).
- **Контракты и график:** `/crm/contracts`, строки `.../contracts/{id}/lines`.
- **Карта:** `GET /crm/map/pins`, объекты компании `.../map/company-objects`.
- **Метрики:** `GET /crm/metrics/summary` (агрегаты в рамках скоупа: OWNER — всё, ADMIN — только назначенные на него сущности / свои задачи).
- **Комиссии менеджеров:** `GET /crm/manager-commissions` (JWT ADMIN/OWNER; опциональный фильтр `managerId`).
- **Филиалы организаций:** CRUD под `/crm/organizations/{id}/branches`.
- **Календарь поставок:** `GET /crm/supplies/calendar` (поля `plannedDate`, `plannedEndDate` на поставке).
- **Веб-аналитика (first-party):** `GET /crm/analytics/summary`, `GET /crm/analytics/events` — сводка и лента по событиям с сайта (JWT ADMIN/OWNER; глобальные агрегаты, без привязки к сущностям CRM).

Менеджер в ТЗ соответствует роли **ADMIN**; руководитель — **OWNER**. Координаты и статусы вводятся вручную, внешние интеграции не используются.

---

## Запуск локально

1. Поднимите PostgreSQL и создайте БД (или используйте существующую).
2. Укажите подключение и при необходимости путь к файлам в `src/main/resources/application.yaml` (`spring.datasource.*`, `app.file.bucket`).
3. Запуск:

```bash
mvn spring-boot:run
```

Сборка JAR:

```bash
mvn clean package
java -jar target/gbcc-website-backend-0.0.1-SNAPSHOT.jar
```

---

## Домены API (кратко)

Детали запросов/ответов, коды ошибок и схемы авторизации — в **Swagger** по тегам:

| Тег | Назначение |
|-----|------------|
| **Auth** | `/auth/login`, `/auth/refresh`, `/auth/logout` — выдача JWT в HttpOnly cookies |
| **Accounts** | Регистрация, CRUD, **аналитика клиента** (`GET/PUT /account/customer/{id}/analytics`), **staff-profile**, CRM-привязки (`PATCH /account/customer/{id}`), блокировка |
| **Products** | Каталог: CRUD, поиск с фильтрами, **сгруппированный поиск** (`/product/search/grouped`), классы, фото товара, **остатки** (`GET /product/stock`, `PATCH /product/{id}/stock`) |
| **Files** | Загрузка multipart, поиск метаданных, скачивание/удаление по UUID |
| **News** | Новости; чтение публично; создание/редактирование — ADMIN/OWNER |
| **Promotions** | Акции (скидки по правилам); список публично; изменение — ADMIN/OWNER |
| **Orders** | Заказы: история (**GET /order**), карточка, **PATCH /order/{id}** (менеджер), смена статуса; резерв/восстановление остатков; фильтр `crmOrganizationId` |
| **Support** | Обращения в администрацию; гости с токеном `X-Support-Token` |
| **Contact requests** | Заявки с формы на сайте: публичная отправка; список и «взять на изучение» — ADMIN/OWNER |
| **CRM — Organizations** … **CRM — Metrics** | Модуль CRM под префиксом `/crm/**` (см. раздел **CRM API** выше) |
| **Site analytics** | Публично: `POST /analytics/collect` — пакет событий (без JWT). Подробности — ниже. |

---

## Для фронтенд-разработчика

### Базовый URL

По умолчанию API: `http://localhost:8080` (см. `app.swagger.servers` в `application.yaml`).

### CORS

В проекте **нет** глобальной настройки CORS. Для отдельного origin фронтенда добавьте `WebMvcConfigurer` + `CorsConfiguration` или настройте прокси (nginx / devServer), чтобы браузерные запросы шли **с того же origin** или с разрешённого хоста.

### Аутентификация

- После **POST `/auth/login`** сервер устанавливает **HttpOnly** cookies:
  - `GBCC_ACCESS_TOKEN` — path `/`, используется для доступа к защищённым эндпоинтам.
  - `GBCC_REFRESH_TOKEN` — path `/auth`, для **POST `/auth/refresh`**.
- Клиент с `credentials: 'include'` (fetch) или `withCredentials: true` (axios) **автоматически** отправляет cookies на тот же origin.
- **Не полагайтесь** на чтение access token из JS, если cookie HttpOnly (так и задумано).

### Защищённые запросы

Для ручных тестов в Postman/Insomnia: добавьте cookie `GBCC_ACCESS_TOKEN=<значение>` (скопировать из DevTools после логина) или используйте Swagger **Authorize** → схема `accessTokenCookie`.

### Пагинация (Spring Data)

Списочные GET принимают стандартные параметры: `page` (0-based), `size`, `sort` (например `sort=createdAt,desc`).

### Веб-аналитика (first-party)

- **Сбор:** `POST /analytics/collect` — без авторизации, ответ **204**. Тело JSON:
  - `visitorId`, `sessionId` — UUID (на уровне пакета).
  - `events` — массив (лимит длины: `app.analytics.max-batch-size`, по умолчанию 50). Элемент: `occurredAt` (ISO-8601), `eventType` (строка до 32 символов, например `PAGE_VIEW`), `path` (желательно с ведущим `/`; иначе сервер добавит), опционально `referrer`, `metadata` (JSON-объект: поля и значения примитивов/вложенных объектов; суммарная длина сериализованного JSON ограничена `app.analytics.max-metadata-chars`; в БД хранится как текст).
- **DNT:** при заголовке `DNT: 1` и `app.analytics.honor-dnt: true` события **не сохраняются** (всё равно 204).
- **Cookie:** при `app.analytics.cookie-enabled: true` сервер может добавить `Set-Cookie` (HttpOnly, имя из `app.analytics.cookie-name`, по умолчанию `GBCC_VISITOR_ID`), если cookie нет или не совпадает с `visitorId` в теле. Флаги `Secure` и TTL — из `app.analytics.*` (для `Secure` при отсутствии значения используется `app.jwt.cookie-secure`).
- **CRM:** `GET /crm/analytics/summary?from=&to=` и `GET /crm/analytics/events?from=&to=&page=&size=` — только с JWT **ADMIN/OWNER**; фильтр по времени приёма на сервере `received_at`, полуинтервал `[from, to)`.

Полный набор настроек — префикс `app.analytics` в `application.yaml`.

### Ошибки

Тело ошибок — **`ApiErrorResponse`**: `timestamp`, `status`, `error`, `message`, `path`, опционально `fields` для валидации.

### Поддержка без регистрации

1. **POST `/support/conversations`** с `message`, `guestName`, `guestEmail`.
2. Сохранить **`guestAccessToken`** из ответа.
3. Для **GET/POST** по этому диалогу передавать заголовок **`X-Support-Token: <guestAccessToken>`** (в Swagger — схема `supportGuestToken`).

### Заявки с формы «Свяжитесь с нами»

1. **POST `/contact-requests`** — публично: `name`, `email`, опционально `phone`, `message`, `consentProcessing: true` (согласие на обработку персональных данных).
2. Администраторы (**ADMIN/OWNER**, JWT): **GET `/contact-requests`** — список с пагинацией; опционально `onlyUnassigned=true`, `query` — поиск по имени, email, телефону и тексту сообщения.
3. **POST `/contact-requests/{id}/take`** — назначить заявку на себя (если уже занята другим админом — ошибка 409; **OWNER** может переназначить).
4. **POST `/contact-requests/{id}/release`** — снять назначение (сам назначенный админ или **OWNER**).

### Каталог и заказы

- Каталог: чтение и поиск товаров — по правилам `SecurityConfig` (часто без JWT). **Создание и изменение товаров и фото** (`POST`/`PUT`/`PATCH`/`DELETE` под `/product/**`) — только с JWT ролей **ADMIN** или **OWNER** (проверка и в Security, и в `ProductServiceImpl`).
- В карточке товара (`ProductResponseDto`): поля **originalPrice**, **finalPrice**, **priceLabel**, **priceLabelHint** (маркетплейс-ценообразование), **stockQuantity**. При создании/изменении можно задать `stockQuantity` (по умолчанию 0).
- **GET `/product/stock`** — сводка остатков (ADMIN/OWNER). **PATCH `/product/{id}/stock`** — изменение остатка.
- **POST `/order`** — только авторизованный **CUSTOMER** с JWT cookie; в теле — **paymentMethod**, опционально **deliveryFee**, **crmOrganizationId**; при создании **резервируется** `stockQuantity`. Отмена до `DELIVERED` **восстанавливает** остаток.
- **GET `/order`** — история заказов (пагинация); для ADMIN/OWNER — фильтр **`crmOrganizationId`**. В записи: контакты, **deliveryFee**, **netTotal**, **crmOrganizationId**, **displayNumber**, статусы, суммы, окно доставки, чек, позиции.
- **PATCH `/order/{orderId}`** — правка менеджером (контакты, адрес, комментарий, `deliveryFee`, `crmOrganizationId`) до финальных статусов.

### Breaking changes (миграции V030–V040)

| Изменение | Действие для клиентов API |
|-----------|---------------------------|
| Удалена таксономия **product_type** | Убрать `typeName` / `typeIds` из запросов и UI; уникальность товара — по классу + серии + бренду. Scope акций **`PRODUCT_TYPE`** мигрирован в **`PRODUCT`**. |
| Поле **stockQuantity** | Заказ с `quantity` > остатка → **400**. Новые товары без `stockQuantity` получают 0. |
| Расширение заказа | Новые поля в ответе/запросе: контакты, `deliveryFee`, `netTotal`, `crmOrganizationId`. |
| Аккаунт ↔ CRM | `crmOrganizationId`, `broughtByManagerId` на клиенте; **PATCH `/account/customer/{id}`** для привязки. |
| Реферальная комиссия | **7%** за первый доставленный заказ приглашённого, **2%** за последующие (`app.referral.commission-percent-*`). В `referral_commission` — `commission_percent`, `client_type`. |
| Комиссия менеджера-привлечёнца | Таблица `manager_referral_commission`; **GET `/crm/manager-commissions`**. |
| CRM организации | Поле **inn**; CRUD **филиалов** `/crm/organizations/{id}/branches`. |
| CRM контакты | **positionTitle**, **socialLinks** (JSON). |
| CRM поставки | **plannedDate**, **plannedEndDate**; **GET `/crm/supplies/calendar`**. |
| Staff | **GET/PUT `/account/{id}/staff-profile`**. |
| CRM взаимодействия | Поле **interactionType**; фильтр `interactionType` в **GET `/crm/interactions`**. |

### Поиск в мобильном меню (блоки «Категория» / «Оборудование»)

- **GET `/product/search/grouped?q=...`** — в ответе: **categories** (классы `product_class`, по имени), **products** (товары, тот же смысл поиска, что **GET `/product?q=...`**), счётчики **totalCategoryCount**, **totalProductCount**, **totalCount** (сумма), и превью в списках с лимитами `categoryLimit` (по умолчанию 5) и `productLimit` (по умолчанию 10).
- Для экрана «Все результаты» используйте обычный **GET `/product`** с тем же `q` и пагинацией `page` / `size`.

### Страница каталога (фильтры и сортировка)

- **GET `/product/classes`** — список категорий с `id` и `name` для сайдбара и хлебных крошек.
- **GET `/product`** — общее число позиций в **`PageResponse.totalElements`** (число в скобках у заголовка).
- Фильтры: **`classId`** (категория), **`minPrice`** / **`maxPrice`**, **`seriesIds`** (повторять query-параметр для нескольких UUID), **`minHeightMm`** / **`maxHeightMm`** (диапазон высоты в мм), **`q`**, **`isActive=true`** для витрины.
- Сортировка через **`sort`**: например `popularityScore,desc` (поле **popularity_score**; по умолчанию 0, при необходимости обновляйте на бэкенде), `price,asc|desc`, `createdAt,desc` (новизна), `isActive,desc` (активные выше).

---

## Flyway

Миграции: `classpath:db/migration`. Именование: `V###__description.sql`.

На продакшене рекомендуется **отключить** `spring.flyway.out-of-order` или строго выдерживать порядок версий.

---

## Валидация и контракт ошибок

- Входные DTO: Jakarta Validation (`@Valid`).
- Перекрёстные проверки — в сервисах (`ResponseStatusException` → обработчик нормализует ответ).

---

## Тесты

Обычный прогон (без Docker): unit + WebMvc, **без** поднятия PostgreSQL в контейнере:

```bash
mvn test
```

Полный прогон с **интеграционными** тестами (нужна **PostgreSQL** с применёнными миграциями Flyway):

- URL по умолчанию в `src/test/resources/application-integration-external.yaml`: **`jdbc:postgresql://localhost:5555/gbcc`** (как в `docker-compose.yaml` в корне: `docker compose up -d`).
- Если БД на другом порту (например **5510**, как в `application.yaml` приложения): задайте `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5510/gbcc` перед `mvn verify -Pintegration-tests`.

```bash
mvn verify -Pintegration-tests
```

Отчёт покрытия **JaCoCo** после `mvn verify`:

- `target/site/jacoco/index.html`
- Минимальная доля покрытых строк задаётся свойством `jacoco.minimum.line.ratio` в `pom.xml` (проверка на фазе `verify`).

Профиль `test` подхватывает `src/test/resources/application-test.yaml` (секрет JWT и owner для инициализации в интеграционных сценариях). Подключение к БД в IT — `src/test/resources/application-integration-external.yaml` (профиль `integration-external`); при необходимости переопределите `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

#### `Failed to resolve org.junit.vintage:junit-vintage-engine:6.0.3`

- В **Spring Boot 4** `spring-boot-starter-test` уже **не** тянет Vintage; тесты в проекте на **JUnit Jupiter**. Версия `6.0.3` задаётся через **JUnit BOM** из родительского POM — IDE иногда показывает «не найдено», хотя `mvn test` проходит.
- **Что сделать:** в IDE — **Reload All Maven Projects** (и при необходимости *Invalidate Caches*). Убедиться, что в `settings.xml` есть доступ к **Maven Central** (или корпоративное зеркало уже проксирует `org/junit/vintage/junit-vintage-engine/6.0.3`).
- Проверка загрузки артефакта: `mvn dependency:get -Dartifact=org.junit.vintage:junit-vintage-engine:6.0.3`.

### Процесс исправления багов (red → green → refactor)

1. **Red** — тест воспроизводит ошибку или фиксирует ожидаемое поведение.
2. **Green** — минимальное изменение кода до прохождения теста.
3. **Refactor** — упрощение без смены контракта API.

### Покрытие: куда смотреть дальше

См. [docs/TEST_COVERAGE_GAPS.md](docs/TEST_COVERAGE_GAPS.md).

Только компиляция:

```bash
mvn -DskipTests compile
```

---

## Версия API

Версия OpenAPI в описании задаётся в `app.swagger.version` (`application.yaml`); при изменении контракта обновляйте версию и при необходимости changelog в репозитории.
