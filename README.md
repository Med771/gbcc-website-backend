# GBCC Website Backend

REST API для сайта GBCC: каталог товаров, файлы, новости, акции (скидки), заказы, учётные записи с JWT в cookies, обращения в поддержку (в т.ч. от гостей), заявки с формы обратной связи («Свяжитесь с нами»).

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
- `logic/*` — домены: `auth`, `account`, `product`, `file`, `news`, `promotion`, `order`, `support`, `contactrequest`
- `model` — общие enum, DTO ошибок, базовые сущности
- `resources/db/migration` — SQL миграции Flyway (`V###__description.sql`)
- `test` — unit-тесты (Mockito), WebMvc-срезы (`@GbccWebMvcTest`), интеграционные тесты с Testcontainers (тег `requires-docker`)
- `docs/TEST_COVERAGE_GAPS.md` — приоритеты покрытия и пробелы

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
| **Accounts** | Регистрация клиента, CRUD по аккаунтам, поиск, блокировка |
| **Products** | Каталог: CRUD, поиск с фильтрами, **сгруппированный поиск** (`/product/search/grouped`) для мобильного меню, классы, фото товара |
| **Files** | Загрузка multipart, поиск метаданных, скачивание/удаление по UUID |
| **News** | Новости; чтение публично; создание/редактирование — ADMIN/OWNER |
| **Promotions** | Акции (скидки по правилам); список публично; изменение — ADMIN/OWNER |
| **Orders** | Заказы: история (**GET /order**), карточка с номером, оплатой, окном доставки, чеком; CUSTOMER создаёт; менеджеры меняют статус |
| **Support** | Обращения в администрацию; гости с токеном `X-Support-Token` |
| **Contact requests** | Заявки с формы на сайте: публичная отправка; список и «взять на изучение» — ADMIN/OWNER |

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

- Товары и файлы: публичные операции см. Swagger (часть мутаций может быть открыта — сверяйтесь с `SecurityConfig`).
- **POST `/order`** — только авторизованный **CUSTOMER** с JWT cookie; в теле — **paymentMethod** (`CARD_ONLINE` или `CARD_OR_ON_RECEIPT` и т.д., см. Swagger).
- **GET `/order`** — история заказов текущего клиента (пагинация); в каждой записи: **displayNumber** (короткий номер), **statusLabel**, **paymentMethodLabel**, суммы, **estimatedDeliveryAt** / **estimatedDeliveryEnd**, **receiptUrl**, позиции с названиями класса/серии/типа товара.

### Поиск в мобильном меню (блоки «Категория» / «Оборудование»)

- **GET `/product/search/grouped?q=...`** — в ответе: **categories** (классы `product_class`, по имени), **products** (товары, тот же смысл поиска, что **GET `/product?q=...`**), счётчики **totalCategoryCount**, **totalProductCount**, **totalCount** (сумма), и превью в списках с лимитами `categoryLimit` (по умолчанию 5) и `productLimit` (по умолчанию 10).
- Для экрана «Все результаты» используйте обычный **GET `/product`** с тем же `q` и пагинацией `page` / `size`.

### Страница каталога (фильтры и сортировка)

- **GET `/product/classes`** — список категорий с `id` и `name` для сайдбара и хлебных крошек.
- **GET `/product`** — общее число позиций в **`PageResponse.totalElements`** (число в скобках у заголовка).
- Фильтры: **`classId`** (категория), **`minPrice`** / **`maxPrice`**, **`seriesIds`** / **`typeIds`** (повторять query-параметр для нескольких UUID — чекбоксы серий/типов), **`minHeightMm`** / **`maxHeightMm`** (диапазон высоты в мм), **`q`**, **`isActive=true`** для витрины.
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

Полный прогон с **интеграционными** тестами (Testcontainers PostgreSQL — нужен **Docker**):

```bash
mvn verify -Pintegration-tests
```

Отчёт покрытия **JaCoCo** после `mvn verify`:

- `target/site/jacoco/index.html`
- Минимальная доля покрытых строк задаётся свойством `jacoco.minimum.line.ratio` в `pom.xml` (проверка на фазе `verify`).

Профиль `test` подхватывает `src/test/resources/application-test.yaml` (секрет JWT и owner для инициализации в интеграционных сценариях).

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
