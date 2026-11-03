package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class VirtualAccountDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDTO.class);
        VirtualAccountDTO virtualAccountDTO1 = new VirtualAccountDTO();
        virtualAccountDTO1.setId(1L);
        VirtualAccountDTO virtualAccountDTO2 = new VirtualAccountDTO();
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
        virtualAccountDTO2.setId(virtualAccountDTO1.getId());
        assertThat(virtualAccountDTO1).isEqualTo(virtualAccountDTO2);
        virtualAccountDTO2.setId(2L);
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
        virtualAccountDTO1.setId(null);
        assertThat(virtualAccountDTO1).isNotEqualTo(virtualAccountDTO2);
    }
}
