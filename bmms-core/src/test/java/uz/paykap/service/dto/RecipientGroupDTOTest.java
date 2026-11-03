package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class RecipientGroupDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RecipientGroupDTO.class);
        RecipientGroupDTO recipientGroupDTO1 = new RecipientGroupDTO();
        recipientGroupDTO1.setId(1L);
        RecipientGroupDTO recipientGroupDTO2 = new RecipientGroupDTO();
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
        recipientGroupDTO2.setId(recipientGroupDTO1.getId());
        assertThat(recipientGroupDTO1).isEqualTo(recipientGroupDTO2);
        recipientGroupDTO2.setId(2L);
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
        recipientGroupDTO1.setId(null);
        assertThat(recipientGroupDTO1).isNotEqualTo(recipientGroupDTO2);
    }
}
