package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class RecipientFileSchemaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientFileSchemaDTO.class);
        RecipientFileSchemaDTO recipientFileSchemaDTO1 = new RecipientFileSchemaDTO();
        recipientFileSchemaDTO1.setId(1L);
        RecipientFileSchemaDTO recipientFileSchemaDTO2 = new RecipientFileSchemaDTO();
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO2.setId(recipientFileSchemaDTO1.getId());
        assertThat(recipientFileSchemaDTO1).isEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO2.setId(2L);
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
        recipientFileSchemaDTO1.setId(null);
        assertThat(recipientFileSchemaDTO1).isNotEqualTo(recipientFileSchemaDTO2);
    }
}
