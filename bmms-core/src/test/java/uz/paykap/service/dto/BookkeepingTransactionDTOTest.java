package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class BookkeepingTransactionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BookkeepingTransactionDTO.class);
        BookkeepingTransactionDTO bookkeepingTransactionDTO1 = new BookkeepingTransactionDTO();
        bookkeepingTransactionDTO1.setId(1L);
        BookkeepingTransactionDTO bookkeepingTransactionDTO2 = new BookkeepingTransactionDTO();
        assertThat(bookkeepingTransactionDTO1).isNotEqualTo(bookkeepingTransactionDTO2);
        bookkeepingTransactionDTO2.setId(bookkeepingTransactionDTO1.getId());
        assertThat(bookkeepingTransactionDTO1).isEqualTo(bookkeepingTransactionDTO2);
        bookkeepingTransactionDTO2.setId(2L);
        assertThat(bookkeepingTransactionDTO1).isNotEqualTo(bookkeepingTransactionDTO2);
        bookkeepingTransactionDTO1.setId(null);
        assertThat(bookkeepingTransactionDTO1).isNotEqualTo(bookkeepingTransactionDTO2);
    }
}
