package backend.website.gbcc.logic.referral;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralCodeNormalizerTest {

    @Test
    void normalize_stripsHashAndUppercases() {
        assertThat(ReferralCodeNormalizer.normalize("  #abc-12xy  "))
                .contains("ABC-12XY");
    }

    @Test
    void normalize_insertsHyphenForSevenChars() {
        assertThat(ReferralCodeNormalizer.normalize("AB2CD3F"))
                .contains("AB2-CD3F");
    }

    @Test
    void normalize_invalid_returnsEmpty() {
        assertThat(ReferralCodeNormalizer.normalize("")).isEmpty();
        assertThat(ReferralCodeNormalizer.normalize("AB")).isEmpty();
        assertThat(ReferralCodeNormalizer.normalize("ABCD-EFGH")).isEmpty();
    }
}
