package backend.website.gbcc.logic.staff;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class StaffDisplayIdGenerator {

    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int SUFFIX_LEN = 6;

    private final SecureRandom random = new SecureRandom();

    public String generateUniqueId(java.util.function.Predicate<String> isTaken) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String id = "EMP-" + randomSegment(SUFFIX_LEN);
            if (!isTaken.test(id)) {
                return id;
            }
        }
        throw new IllegalStateException("Could not allocate staff display id");
    }

    private String randomSegment(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = ALPHANUM[random.nextInt(ALPHANUM.length)];
        }
        return new String(buf);
    }
}
