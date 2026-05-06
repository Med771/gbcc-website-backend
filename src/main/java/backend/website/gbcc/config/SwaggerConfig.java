package backend.website.gbcc.config;

import backend.website.gbcc.config.property.SwaggerProperty;
import backend.website.gbcc.helper.AuthCookieHelper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerProperty swaggerProperty;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerProperty.getTitle())
                        .description(swaggerProperty.getDescription())
                        .version(swaggerProperty.getVersion()))
                .servers(swaggerProperty.getServers().stream()
                        .map(serv -> new Server()
                                .url(serv.getUrl())
                                .description(serv.getDescription()))
                        .toList())
                .components(new Components()
                        .addSecuritySchemes(OpenApiConstants.SECURITY_ACCESS_COOKIE,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name(AuthCookieHelper.ACCESS_COOKIE)
                                        .description("""
                                                JWT access token (HttpOnly cookie). Выставляется после POST /auth/login.
                                                В Swagger UI: Authorize → значение токена (если копируете из DevTools) или вход через браузер на том же origin.
                                                """)
                        )
                        .addSecuritySchemes(OpenApiConstants.SECURITY_SUPPORT_GUEST_TOKEN,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(OpenApiConstants.SUPPORT_TOKEN_HEADER_NAME)
                                        .description("""
                                                Токен гостя для обращения в поддержку без аккаунта.
                                                Возвращается в теле ответа POST /support/conversations (поле guestAccessToken).
                                                """)
                        )
                )
                .tags(List.of(
                        new Tag().name("Auth").description("Вход, обновление и выход. Токены в HttpOnly cookies."),
                        new Tag().name("Accounts").description("Учётные записи: регистрация клиента, CRUD по ролям, поиск, блокировка."),
                        new Tag().name("Products").description("Каталог товаров: создание, поиск, фото, классы."),
                        new Tag().name("Files").description("Загрузка и выдача файлов по UUID-ключу."),
                        new Tag().name("News").description("Новости: публичное чтение; создание/редактирование — ADMIN/OWNER."),
                        new Tag().name("Promotions").description("Акции (скидки по правилам и охвату). Публичный список; изменение — ADMIN/OWNER."),
                        new Tag().name("Orders").description("Заказы: оформление клиентом, просмотр, смена статуса (менеджеры)."),
                        new Tag().name("Support").description("Обращения в администрацию: гости (токен), клиенты (JWT), ответы админов."),
                        new Tag().name("Referrals").description("Реферальная программа: клики, коды, бонусы с заказов, вывод; отчёты для ADMIN/OWNER."),
                        new Tag().name("CRM — Organizations").description("Карточки организаций (B2B), контакты, история изменений контактов. JWT ADMIN/OWNER."),
                        new Tag().name("CRM — Interactions").description("Журнал взаимодействий (звонки, встречи): дата, результат, следующий шаг."),
                        new Tag().name("CRM — Tasks").description("Задачи и перезвоны: срок, исполнитель, фильтры «ближайшие / просроченные»."),
                        new Tag().name("CRM — Leads").description("Холодная база лидов, статусы, конвертация в организацию."),
                        new Tag().name("CRM — Supplies").description("Поставки и долгосрочные контракты (ручные статусы и график)."),
                        new Tag().name("CRM — Map").description("Точки на карте (координаты вводятся вручную)."),
                        new Tag().name("CRM — Metrics").description("Сводные метрики по CRM (только чтение, без внешних систем).")
                ));
    }
}
