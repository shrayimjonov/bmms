package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReconcileSummaryMapperTest {

    private ReconcileSummaryMapper reconcileSummaryMapper;

    @BeforeEach
    public void setUp() {
        reconcileSummaryMapper = new ReconcileSummaryMapperImpl();
    }
}
