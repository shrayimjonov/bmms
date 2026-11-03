package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class RecipientFileSchemaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientFileSchema.class);
        RecipientFileSchema recipientFileSchema1 = new RecipientFileSchema();
        recipientFileSchema1.setId(1L);
        RecipientFileSchema recipientFileSchema2 = new RecipientFileSchema();
        recipientFileSchema2.setId(recipientFileSchema1.getId());
        assertThat(recipientFileSchema1).isEqualTo(recipientFileSchema2);
        recipientFileSchema2.setId(2L);
        assertThat(recipientFileSchema1).isNotEqualTo(recipientFileSchema2);
        recipientFileSchema1.setId(null);
        assertThat(recipientFileSchema1).isNotEqualTo(recipientFileSchema2);
    }
}
