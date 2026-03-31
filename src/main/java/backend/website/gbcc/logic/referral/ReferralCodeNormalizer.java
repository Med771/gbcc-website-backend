package backend.website.gbcc.logic.referral;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

final class ReferralCodeNormalizer {

    private static final Pattern CANONICAL = Pattern.compile("^[A-Z0-9]{3}-[A-Z0-9]{4}$");

    private ReferralCodeNormalizer() {
    }

    static Optional<String> normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.startsWith("#")) {
            s = s.substring(1).trim();
        }
        s = s.replaceAll("\\s+", "");
        if (!s.contains("-") && s.length() == 7 && s.matches("[A-Z0-9]{7}")) {
            s = s.substring(0, 3) + "-" + s.substring(3);
        }
        if (CANONICAL.matcher(s).matches()) {
            return Optional.of(s);
        }
        return Optional.empty();
    }
}
