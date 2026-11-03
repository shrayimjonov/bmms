package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class VirtualAccountDetailsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDetailsDTO.class);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO1 = new VirtualAccountDetailsDTO();
        virtualAccountDetailsDTO1.setId(1L);
        VirtualAccountDetailsDTO virtualAccountDetailsDTO2 = new VirtualAccountDetailsDTO();
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO2.setId(virtualAccountDetailsDTO1.getId());
        assertThat(virtualAccountDetailsDTO1).isEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO2.setId(2L);
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
        virtualAccountDetailsDTO1.setId(null);
        assertThat(virtualAccountDetailsDTO1).isNotEqualTo(virtualAccountDetailsDTO2);
    }
}
