package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class PrefixTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prefix.class);
        Prefix prefix1 = new Prefix();
        prefix1.setId(1L);
        Prefix prefix2 = new Prefix();
        prefix2.setId(prefix1.getId());
        assertThat(prefix1).isEqualTo(prefix2);
        prefix2.setId(2L);
        assertThat(prefix1).isNotEqualTo(prefix2);
        prefix1.setId(null);
        assertThat(prefix1).isNotEqualTo(prefix2);
    }
}
