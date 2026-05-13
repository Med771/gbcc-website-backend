package backend.website.gbcc.integration.fixtures;

/**
 * Типовые JSON-тела для интеграционных сценариев (один стиль данных для всех IT).
 */
public final class IntegrationExampleRequests {

    private IntegrationExampleRequests() {
    }

    public static String customerRegisterJson(String phone, String email) {
        return """
                {
                  "firstName": "Integration",
                  "lastName": "User",
                  "patronymic": null,
                  "phone": "%s",
                  "email": "%s",
                  "password": "Password123!",
                  "inviteCode": null
                }
                """.formatted(phone, email);
    }

    public static String loginJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}
