package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class BookkeepingTemplateTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BookkeepingTemplate.class);
        BookkeepingTemplate bookkeepingTemplate1 = new BookkeepingTemplate();
        bookkeepingTemplate1.setId(1L);
        BookkeepingTemplate bookkeepingTemplate2 = new BookkeepingTemplate();
        bookkeepingTemplate2.setId(bookkeepingTemplate1.getId());
        assertThat(bookkeepingTemplate1).isEqualTo(bookkeepingTemplate2);
        bookkeepingTemplate2.setId(2L);
        assertThat(bookkeepingTemplate1).isNotEqualTo(bookkeepingTemplate2);
        bookkeepingTemplate1.setId(null);
        assertThat(bookkeepingTemplate1).isNotEqualTo(bookkeepingTemplate2);
    }
}
