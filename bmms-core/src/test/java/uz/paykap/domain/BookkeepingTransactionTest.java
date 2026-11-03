package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class BookkeepingTransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BookkeepingTransaction.class);
        BookkeepingTransaction bookkeepingTransaction1 = new BookkeepingTransaction();
        bookkeepingTransaction1.setId(1L);
        BookkeepingTransaction bookkeepingTransaction2 = new BookkeepingTransaction();
        bookkeepingTransaction2.setId(bookkeepingTransaction1.getId());
        assertThat(bookkeepingTransaction1).isEqualTo(bookkeepingTransaction2);
        bookkeepingTransaction2.setId(2L);
        assertThat(bookkeepingTransaction1).isNotEqualTo(bookkeepingTransaction2);
        bookkeepingTransaction1.setId(null);
        assertThat(bookkeepingTransaction1).isNotEqualTo(bookkeepingTransaction2);
    }
}
