package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ReconcileSummaryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReconcileSummary.class);
        ReconcileSummary reconcileSummary1 = new ReconcileSummary();
        reconcileSummary1.setId(1L);
        ReconcileSummary reconcileSummary2 = new ReconcileSummary();
        reconcileSummary2.setId(reconcileSummary1.getId());
        assertThat(reconcileSummary1).isEqualTo(reconcileSummary2);
        reconcileSummary2.setId(2L);
        assertThat(reconcileSummary1).isNotEqualTo(reconcileSummary2);
        reconcileSummary1.setId(null);
        assertThat(reconcileSummary1).isNotEqualTo(reconcileSummary2);
    }
}
