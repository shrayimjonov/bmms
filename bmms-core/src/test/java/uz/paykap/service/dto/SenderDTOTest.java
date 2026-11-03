package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class SenderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SenderDTO.class);
        SenderDTO senderDTO1 = new SenderDTO();
        senderDTO1.setId(1L);
        SenderDTO senderDTO2 = new SenderDTO();
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
        senderDTO2.setId(senderDTO1.getId());
        assertThat(senderDTO1).isEqualTo(senderDTO2);
        senderDTO2.setId(2L);
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
        senderDTO1.setId(null);
        assertThat(senderDTO1).isNotEqualTo(senderDTO2);
    }
}
