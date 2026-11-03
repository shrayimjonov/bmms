package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class RecipientGroupTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientGroup.class);
        RecipientGroup recipientGroup1 = new RecipientGroup();
        recipientGroup1.setId(1L);
        RecipientGroup recipientGroup2 = new RecipientGroup();
        recipientGroup2.setId(recipientGroup1.getId());
        assertThat(recipientGroup1).isEqualTo(recipientGroup2);
        recipientGroup2.setId(2L);
        assertThat(recipientGroup1).isNotEqualTo(recipientGroup2);
        recipientGroup1.setId(null);
        assertThat(recipientGroup1).isNotEqualTo(recipientGroup2);
    }
}
