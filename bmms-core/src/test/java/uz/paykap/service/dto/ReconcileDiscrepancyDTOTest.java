package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ReconcileDiscrepancyDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileDiscrepancyDTO.class);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO1 = new ReconcileDiscrepancyDTO();
        reconcileDiscrepancyDTO1.setId(1L);
        ReconcileDiscrepancyDTO reconcileDiscrepancyDTO2 = new ReconcileDiscrepancyDTO();
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO2.setId(reconcileDiscrepancyDTO1.getId());
        assertThat(reconcileDiscrepancyDTO1).isEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO2.setId(2L);
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
        reconcileDiscrepancyDTO1.setId(null);
        assertThat(reconcileDiscrepancyDTO1).isNotEqualTo(reconcileDiscrepancyDTO2);
    }
}
