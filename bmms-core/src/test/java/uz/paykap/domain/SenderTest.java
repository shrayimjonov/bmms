package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class SenderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sender.class);
        Sender sender1 = new Sender();
        sender1.setId(1L);
        Sender sender2 = new Sender();
        sender2.setId(sender1.getId());
        assertThat(sender1).isEqualTo(sender2);
        sender2.setId(2L);
        assertThat(sender1).isNotEqualTo(sender2);
        sender1.setId(null);
        assertThat(sender1).isNotEqualTo(sender2);
    }
}
