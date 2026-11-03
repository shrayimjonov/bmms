package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class DataAdapterHistoryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DataAdapterHistory.class);
        DataAdapterHistory dataAdapterHistory1 = new DataAdapterHistory();
        dataAdapterHistory1.setId(1L);
        DataAdapterHistory dataAdapterHistory2 = new DataAdapterHistory();
        dataAdapterHistory2.setId(dataAdapterHistory1.getId());
        assertThat(dataAdapterHistory1).isEqualTo(dataAdapterHistory2);
        dataAdapterHistory2.setId(2L);
        assertThat(dataAdapterHistory1).isNotEqualTo(dataAdapterHistory2);
        dataAdapterHistory1.setId(null);
        assertThat(dataAdapterHistory1).isNotEqualTo(dataAdapterHistory2);
    }
}
