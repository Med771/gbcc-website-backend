package backend.website.gbcc.logic.referral;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ReferralCodeGenerator {

    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int PREFIX_LEN = 3;
    private static final int SUFFIX_LEN = 4;

    private final SecureRandom random = new SecureRandom();

    public String generateUniqueCode(java.util.function.Predicate<String> isTaken) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String code = randomSegment(PREFIX_LEN) + "-" + randomSegment(SUFFIX_LEN);
            if (!isTaken.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not allocate referral code");
    }

    private String randomSegment(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = ALPHANUM[random.nextInt(ALPHANUM.length)];
        }
        return new String(buf);
    }
}
