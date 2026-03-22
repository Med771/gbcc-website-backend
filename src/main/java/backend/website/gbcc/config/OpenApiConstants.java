package backend.website.gbcc.config;

/**
 * Имена схем безопасности OpenAPI (springdoc). Должны совпадать с {@link SwaggerConfig}.
 */
public final class OpenApiConstants {

    public static final String SECURITY_ACCESS_COOKIE = "accessTokenCookie";
    public static final String SECURITY_SUPPORT_GUEST_TOKEN = "supportGuestToken";

    /** Заголовок доступа гостя к диалогу поддержки (совпадает с SupportController). */
    public static final String SUPPORT_TOKEN_HEADER_NAME = "X-Support-Token";

    private OpenApiConstants() {
    }
}
