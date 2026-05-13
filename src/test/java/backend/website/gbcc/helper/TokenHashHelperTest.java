package backend.website.gbcc.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashHelperTest {

    private final TokenHashHelper helper = new TokenHashHelper();

    @Test
    void sha256_matchesKnownVector_forAbc() {
        assertThat(helper.sha256("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sha256_isDeterministic_forEmptyString() {
        assertThat(helper.sha256(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
