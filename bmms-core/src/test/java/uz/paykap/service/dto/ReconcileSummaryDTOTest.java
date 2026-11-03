package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ReconcileSummaryDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileSummaryDTO.class);
        ReconcileSummaryDTO reconcileSummaryDTO1 = new ReconcileSummaryDTO();
        reconcileSummaryDTO1.setId(1L);
        ReconcileSummaryDTO reconcileSummaryDTO2 = new ReconcileSummaryDTO();
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO2.setId(reconcileSummaryDTO1.getId());
        assertThat(reconcileSummaryDTO1).isEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO2.setId(2L);
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
        reconcileSummaryDTO1.setId(null);
        assertThat(reconcileSummaryDTO1).isNotEqualTo(reconcileSummaryDTO2);
    }
}
