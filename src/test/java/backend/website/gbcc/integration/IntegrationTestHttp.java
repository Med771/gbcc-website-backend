package backend.website.gbcc.integration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * Вспомогательные методы для HTTP-интеграционных тестов (cookies, заголовки).
 */
public final class IntegrationTestHttp {

    private IntegrationTestHttp() {
    }

    public static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Извлекает первую пару {@code Name=value} из заголовка Set-Cookie для cookie, имя которой начинается с prefix.
     */
    public static String extractCookiePair(List<String> setCookieValues, String namePrefix) {
        if (setCookieValues == null) {
            return null;
        }
        for (String raw : setCookieValues) {
            if (raw != null && raw.startsWith(namePrefix)) {
                return raw.split(";", 2)[0].trim();
            }
        }
        return null;
    }

    public static HttpHeaders jsonHeadersWithCookie(String cookieHeaderValue) {
        HttpHeaders headers = jsonHeaders();
        if (cookieHeaderValue != null && !cookieHeaderValue.isBlank()) {
            headers.add(HttpHeaders.COOKIE, cookieHeaderValue);
        }
        return headers;
    }
}
