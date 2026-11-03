package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ReconcileDiscrepancyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileDiscrepancy.class);
        ReconcileDiscrepancy reconcileDiscrepancy1 = new ReconcileDiscrepancy();
        reconcileDiscrepancy1.setId(1L);
        ReconcileDiscrepancy reconcileDiscrepancy2 = new ReconcileDiscrepancy();
        reconcileDiscrepancy2.setId(reconcileDiscrepancy1.getId());
        assertThat(reconcileDiscrepancy1).isEqualTo(reconcileDiscrepancy2);
        reconcileDiscrepancy2.setId(2L);
        assertThat(reconcileDiscrepancy1).isNotEqualTo(reconcileDiscrepancy2);
        reconcileDiscrepancy1.setId(null);
        assertThat(reconcileDiscrepancy1).isNotEqualTo(reconcileDiscrepancy2);
    }
}
