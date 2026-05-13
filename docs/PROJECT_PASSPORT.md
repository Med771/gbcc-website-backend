# Паспорт проекта: GBCC Website Backend

Документ фиксирует назначение, стек, архитектуру, контракты API, безопасность, данные, тесты и эксплуатацию backend-сервиса сайта GBCC. Версия описания: **2026-05** (сверяйте с `app.swagger.version` в `application.yaml` при изменении контракта).

---

## 1. Идентификация

| Поле | Значение |
|------|----------|
| **Артефакт Maven** | `com.example:gbcc-website-backend:0.0.1-SNAPSHOT` |
| **Корневой пакет** | `backend.website.gbcc` |
| **Точка входа** | `GbccWebsiteBackendApplication` |
| **Назначение** | REST API: каталог и заказы, файлы, новости, акции, аккаунты, JWT в cookies, поддержка (в т.ч. гости), заявки обратной связи и сотрудничества, реферальная программа, **модуль CRM** (`/crm/**`) |
| **Документация для разработчиков** | [README.md](../README.md), [docs/EXPERT_GUIDE.md](EXPERT_GUIDE.md), OpenAPI/Swagger (см. §8), [TEST_COVERAGE_GAPS.md](TEST_COVERAGE_GAPS.md) |

---

## 2. Технологический стек

| Компонент | Версия / примечание |
|-----------|---------------------|
| Java | 21 |
| Spring Boot | 4.0.3 (родительский POM) |
| Spring Web MVC, Security, Data JPA | через стартеры Boot 4 |
| PostgreSQL | драйвер 42.7.10; целевая СУБД 15+ |
| Flyway | 12.0.2 + `flyway-database-postgresql` |
| JWT | JJWT 0.12.6 (api + impl + jackson) |
| API-документация | springdoc-openapi 3.0.1 (`springdoc-openapi-starter-webmvc-ui`) |
| Маппинг DTO | MapStruct 1.6.3 |
| Утилиты | Lombok 1.18.42 |
| Тесты | JUnit Jupiter, Mockito, `spring-boot-starter-webmvc-test`, Spring Security Test |
| Интеграционные тесты | Внешняя PostgreSQL (профили `test` + `integration-external`), см. `application-integration-external.yaml` / `docker-compose.yaml` |
| Покрытие | JaCoCo 0.8.12, проверка минимальной доли строк на `verify` (`jacoco.minimum.line.ratio` в `pom.xml`, сейчас 0.20) |

Сборка: **Maven** (`mvnw` / локальный Maven). Упаковка: исполняемый JAR (`spring-boot-maven-plugin` repackage).

---

## 3. Архитектура и структура кода

### 3.1. Слои

- **HTTP** — `@RestController` в пакете `logic/<домен>/` рядом с сервисами домена.
- **Безопасность** — цепочка фильтров Spring Security + `JwtAuthenticationFilter` (`filter`), чтение access JWT из cookie.
- **Прикладная логика** — интерфейсы `*Service` и реализации `*ServiceImpl`, спецификации JPA (`*Specification`), мапперы MapStruct (`*Mapper`).
- **Данные** — JPA-сущности (`*Entity`), репозитории `JpaRepository` / кастомные запросы.
- **Ошибки** — `GlobalExceptionHandler` (`handler`), единое тело `ApiErrorResponse` (`model/error`).
- **Конфигурация** — `config` (Security, OpenAPI, JPA auditing, свойства JWT и др.).
- **Вспомогательное** — `helper` (JWT, cookies), `tool` (нормализация таксономии и т.п.).

### 3.2. Пакеты верхнего уровня (`backend.website.gbcc`)

| Пакет | Содержимое |
|-------|------------|
| `config` | `SecurityConfig`, `SwaggerConfig`, `JpaAuditingConfig`, `OpenApiConstants`, `config/property/*` |
| `filter` | `JwtAuthenticationFilter` |
| `handler` | `GlobalExceptionHandler` |
| `logic/*` | Домены: `auth`, `account`, `product` (+ `productphoto`, `productseries`), `file`, `news`, `promotion`, `order`, `support`, `contactrequest`, `cooperationrequest`, `referral`, **`crm`** |
| `model` | Роли, принципал, enum домена, DTO ошибок, конвертеры для JPA |
| `resources` | `application.yaml`, `logback-spring.xml`, `db/migration/*.sql` |

### 3.4. Модуль CRM (`logic/crm`)

Префикс HTTP: **`/crm/**`**. Политика доступа: [`CrmAccessPolicy`](../src/main/java/backend/website/gbcc/logic/crm/access/CrmAccessPolicy.java) — только **ADMIN** и **OWNER**; **OWNER** видит все записи CRM; **ADMIN** — организации и лиды, назначенные на него (`assigned_to`), задачи, где он **assignee**, поставки/контракты по «своим» организациям; записи без назначения видит только **OWNER**. Детали — в Swagger по тегам `CRM — …`.

---

В `SecurityConfig` после перечисления правил стоит **`.anyRequest().permitAll()`**. Значит часть операций **не блокируется цепочкой фильтров** и опирается на проверки внутри сервисов (роль, владелец ресурса, гостевой токен поддержки и т.д.). При изменении API необходимо проверять и **HTTP-доступ**, и **сервисные** ограничения.

---

## 4. Роли и идентичность

| Роль (`AccountRole`) | Назначение (кратко) |
|----------------------|---------------------|
| `OWNER` | Высший уровень; сценарии переназначения заявок, админ-реферал и др. по коду сервисов |
| `ADMIN` | Управление аккаунтами (не OWNER), поддержка, заявки, акции и т.д. по доменам |
| `CUSTOMER` | Покупатель: заказы, профиль, реферал как приглашённый/реферер где применимо |

Аутентификация: **JWT в HttpOnly cookie** `GBCC_ACCESS_TOKEN` (path `/`), refresh — `GBCC_REFRESH_TOKEN` (path `/auth`). Константы: `AuthCookieHelper`. Сроки и флаги cookie: `app.jwt.*` (`JwtProperty`).

---

## 5. Карта REST API (базовые пути)

Префикса `/api` нет: корень приложения (по умолчанию порт **8080**). Ниже — маршруты контроллеров; детали параметров и схем — в Swagger.

### 5.1. Auth — `/auth`

| Метод | Путь | Заметка |
|-------|------|---------|
| POST | `/auth/login` | Выдача cookies |
| POST | `/auth/refresh` | Обновление по refresh cookie |
| POST | `/auth/logout` | Завершение сессии |

### 5.2. Accounts — `/account`

| Метод | Путь |
|-------|------|
| POST | `/account/admin` |
| POST | `/account/customer/register` |
| GET | `/account/me` |
| PUT | `/account/me` |
| PUT | `/account/customer/{accountId}` |
| PATCH | `/account/customer/{accountId}/activate` |
| DELETE | `/account/customer/{accountId}` |
| GET | `/account/{accountId}` |
| GET | `/account` |
| PATCH | `/account/{accountId}/block` |
| PATCH | `/account/{accountId}/unblock` |

### 5.3. Products — `/product`

| Метод | Путь |
|-------|------|
| POST | `/product` |
| PUT | `/product/{productId}` |
| PATCH | `/product/{productId}` |
| GET | `/product/classes` |
| GET | `/product/{productId}` |
| GET | `/product/{productId}/similar` |
| GET | `/product/search/grouped` |
| GET | `/product` |
| POST | `/product/{productId}/photos` |
| DELETE | `/product/{productId}/photos?fileId=...` |

### 5.4. Files — `/file`

| Метод | Путь |
|-------|------|
| POST | `/file` (multipart) |
| GET | `/file` |
| GET | `/file/{key}` |
| DELETE | `/file/{key}` |

### 5.5. News — `/news`

| Метод | Путь |
|-------|------|
| POST | `/news` |
| PUT | `/news/{newsId}` |
| PATCH | `/news/{newsId}` |
| GET | `/news/categories` |
| GET | `/news/{newsId}` |
| GET | `/news` |

### 5.6. Promotions — `/promotion`

| Метод | Путь |
|-------|------|
| POST | `/promotion` |
| PUT | `/promotion/{promotionId}` |
| PATCH | `/promotion/{promotionId}` |
| GET | `/promotion/{promotionId}` |
| GET | `/promotion` |
| DELETE | `/promotion/{promotionId}` |

### 5.7. Orders — `/order`

| Метод | Путь |
|-------|------|
| POST | `/order` |
| GET | `/order/{orderId}` |
| GET | `/order` |
| PATCH | `/order/{orderId}/status` |

### 5.8. Support — `/support/conversations`

| Метод | Путь |
|-------|------|
| POST | `/support/conversations` |
| GET | `/support/conversations/my` |
| GET | `/support/conversations` |
| GET | `/support/conversations/{conversationId}` |
| POST | `/support/conversations/{conversationId}/messages` |
| PATCH | `/support/conversations/{conversationId}/status` |

Гостевой доступ к диалогу: заголовок **`X-Support-Token`** (значение из ответа при создании обращения).

### 5.9. Contact requests — `/contact-requests`

| Метод | Путь |
|-------|------|
| POST | `/contact-requests` |
| GET | `/contact-requests` |
| GET | `/contact-requests/{id}` |
| POST | `/contact-requests/{id}/take` |
| POST | `/contact-requests/{id}/release` |

### 5.10. Cooperation requests — `/cooperation-requests`

| Метод | Путь |
|-------|------|
| POST | `/cooperation-requests` |
| GET | `/cooperation-requests` |
| GET | `/cooperation-requests/{id}` |
| POST | `/cooperation-requests/{id}/take` |
| POST | `/cooperation-requests/{id}/release` |

### 5.11. Referral — `/referral`

| Метод | Путь |
|-------|------|
| POST | `/referral/clicks` |
| GET | `/referral/me` |
| GET | `/referral/me/referees` |
| POST | `/referral/me/invite-code` |
| POST | `/referral/me/withdrawals` |
| GET | `/referral/admin/clicks` |
| GET | `/referral/admin/commissions` |
| GET | `/referral/admin/registrations` |
| GET | `/referral/admin/withdrawals` |
| POST | `/referral/admin/withdrawals/{withdrawalId}/pay` |
| POST | `/referral/admin/withdrawals/{withdrawalId}/reject` |

### 5.12. CRM — `/crm`

Сводно (полный список — Swagger): `/crm/organizations`, `/crm/interactions`, `/crm/tasks`, `/crm/leads`, `/crm/supplies`, `/crm/contracts` (+ строки графика), `/crm/map/pins` и CRUD объектов компании, `/crm/metrics/summary`. Заказы витрины остаются в **`/order`** (B2C); поставки CRM — в **`/crm/supplies`** (B2B).

---

## 6. Spring Security (цепочка запросов)

Файл: `SecurityConfig.java`.

- **Отключены**: CSRF, HTTP Basic, form login; сессии **STATELESS**.
- **Публично** (фрагмент правил): Swagger/OpenAPI; `POST /auth/*`; `POST /account/customer/register`; `GET` (и часть мутаций по путям) для промо; создание диалога поддержки и сообщений; `GET` диалога по UUID; `POST /contact-requests`; `POST /cooperation-requests`; `POST /referral/clicks`; чтение каталога **`GET /product`** и связанные публичные GET (см. `SecurityConfig`).
- **Требуют аутентификации** (cookie с валидным JWT и незаблокированным аккаунтом): часть `/news` (POST/PUT/PATCH), мутации `/promotion`, ряд `/support/conversations`, `/contact-requests` (кроме POST корня), `/cooperation-requests` (кроме POST корня), всё под `/referral/**` кроме явно перечисленного, **`POST`/`PUT`/`PATCH`/`DELETE` под `/product/**`** (роль ADMIN/OWNER дополнительно проверяется в `ProductServiceImpl`), **`/order/**`**, **`/account/**`**, **`/crm/**`** (роль ADMIN/OWNER и скоуп проверяются в сервисах CRM).

Точное поведение для спорных путей всегда уточняйте по коду `SecurityConfig` и соответствующего `*ServiceImpl`.

---

## 7. Конфигурация приложения

Основной файл: `src/main/resources/application.yaml`.

| Префикс | Назначение |
|---------|------------|
| `spring.datasource.*` | JDBC PostgreSQL |
| `spring.jpa.*` | Hibernate: `ddl-auto: validate`, `open-in-view: false` |
| `spring.flyway.*` | Миграции; `locations: classpath:db/migration`; в репозитории включён `out-of-order: true` (на проде рекомендуется строгий порядок версий) |
| `app.jwt.*` | Секрет, TTL access/refresh, `cookie-secure`, `cookie-same-site` |
| `app.account.owner.*` | Учётные данные для инициализации owner (см. код старта) |
| `app.file.bucket` | Каталог/«ведро» локального хранения файлов |
| `app.referral.*` | База ссылок, процент комиссии, минимальная сумма вывода |
| `app.swagger.*` | Заголовок/описание OpenAPI, версия API для описания, servers |
| `springdoc.*` | Пути `/v3/api-docs`, `/swagger-ui.html` |

Чувствительные значения в репозитории предназначены для **локальной разработки**; для продакшена используйте переменные окружения или внешний конфиг без секретов в Git.

---

## 8. Документация API (OpenAPI)

- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/v3/api-docs`
- Версия и текст описания задаются в `application.yaml` (`app.swagger.version` и др.).
- В корне репозитория может присутствовать снимок спецификации: `api-docs.yaml` (не заменяет актуальный runtime `/v3/api-docs` без проверки).

Схемы авторизации в UI (см. README): cookie `GBCC_ACCESS_TOKEN`, заголовок гостя поддержки.

---

## 9. База данных и миграции Flyway

Каталог: `src/main/resources/db/migration/`.

| Версия | Файл (назначение по имени) |
|--------|----------------------------|
| V001 | `create__file__table` |
| V002 | `create__product__class__table` |
| V003 | `create__product__series__table` |
| V004 | `create__product__type__table` |
| V005 | `create__product__table` |
| V006 | `create__product__photo__table` |
| V007 | `add__product__search__indexes` |
| V008 | `add__file__key__constraints` |
| V009 | `create__account__table` |
| V010 | `create__refresh__token__table` |
| V011 | `add__product__discount__percent` |
| V012 | `create__order__tables` |
| V013 | `create__news__table` |
| V014 | `create__promotion__table` |
| V015 | `create__support__tables` |
| V016 | `add__news__category` |
| V017 | `create__contact_request__table` |
| V018 | `order__history__fields` |
| V019 | `product__catalog__popularity` |
| V020 | `product__page__content` |
| V021 | `create__cooperation_request__table` |
| V022 | `account__profile__names` |
| V023 | `referral__program` |
| V024 | `crm_organization` (+ контакты, история контактов) |
| V025 | `crm_interaction` |
| V026 | `crm_task` |
| V027 | `crm_lead` (+ привязка задач/взаимодействий к лиду) |
| V028 | `crm_supply`, `crm_contract`, `crm_contract_line`, `crm_company_object`, FK `next_task_id` |

---

## 10. Файловое хранилище

Сервисы файлов (`LocalStorageService` и связанные) используют путь из **`app.file.bucket`** относительно рабочей директории процесса (локальное хранение на диске). Для продакшена обычно требуется согласованный volume/путь в контейнере или вынесение в объектное хранилище (в текущем коде — локальная модель).

---

## 11. Логирование

`logback-spring.xml` + уровни в `application.yaml` (пакет `backend.website.gbcc`, Spring Web, Hibernate SQL/bind).

---

## 12. Тестирование

| Команда | Содержимое прогона |
|---------|-------------------|
| `mvn test` | Unit + WebMvc; IT **исключены** Surefire по имени классов (`GbccWebsiteBackendApplicationTests`, `**/integration/*IntegrationTest.java`) |
| `mvn verify -Pintegration-tests` | Снимает эти `excludes`; нужна доступная PostgreSQL (см. `application-integration-external.yaml`) |
| `mvn verify` | Сборка + тесты + JaCoCo report/check (порог по строкам) |

Профиль тестовых свойств: `src/test/resources/application-test.yaml` (JWT secret и owner для интеграции).

Примеры интеграционных классов: `GbccWebsiteBackendApplicationTests`, `integration/CustomerAuthFlowIntegrationTest` — тег `requires-external-db` для фильтрации в IDE. БД: `application-integration-external.yaml` (по умолчанию порт **5555**, как в `docker-compose.yaml`); при другом порте: `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5510/gbcc`.

Срезы WebMvc: кастомные мета-аннотации/базовые тесты (например `@GbccWebMvcTest` в проекте).

---

## 13. CI/CD

Файл: `.github/workflows/deploy.yml`.

- **Триггеры**: push в ветку `main`, ручной `workflow_dispatch`.
- **Действие**: SSH на хост (`appleboy/ssh-action`), в каталоге `DEPLOY_PATH` выполняется `docker compose up -d --build backend`.
- **Секреты GitHub** (ожидаемые имена): `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_PRIVATE_KEY`, `DEPLOY_PATH`; опционально нестандартный порт SSH — см. комментарии в workflow.

Отдельного workflow для `mvn test` в репозитории может не быть — уточняйте по актуальному `.github/workflows/`.

---

## 14. Ограничения и интеграция с фронтендом

- **CORS** в проекте не настроен глобально: для кросс-доменных браузерных запросов нужен прокси с общим origin или явная конфигурация CORS (см. README).
- Запросы с cookies: **`credentials: 'include'`** (fetch) / **`withCredentials: true`** (axios) для того же origin.
- Пагинация Spring Data: `page`, `size`, `sort`.
- Ошибки: тело **`ApiErrorResponse`** (`timestamp`, `status`, `error`, `message`, `path`, опционально `fields`).

---

## 15. Связанные артефакты в репозитории

| Путь | Назначение |
|------|------------|
| [README.md](../README.md) | Запуск, сценарии для фронта, тесты, FAQ по JUnit Vintage |
| [docs/TEST_COVERAGE_GAPS.md](TEST_COVERAGE_GAPS.md) | Пробелы покрытия |
| [docs/TZ_CRM_BACKEND_ALIGNMENT.md](TZ_CRM_BACKEND_ALIGNMENT.md) | Сверка ТЗ (CRM) с реализованным срезом в этом backend |
| [api-docs.yaml](../api-docs.yaml) | Статический снимок OpenAPI (при наличии) |
| [pom.xml](../pom.xml) | Зависимости, JaCoCo, профили |

---

## 16. Сопровождение паспорта

При значимых изменениях обновляйте этот файл: новые домены и миграции, изменения `SecurityConfig`, новые секреты/переменные окружения, версии стека в `pom.xml`, контракт ошибок и OpenAPI.
