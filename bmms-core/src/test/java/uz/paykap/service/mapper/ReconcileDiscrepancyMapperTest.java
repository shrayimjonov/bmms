package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReconcileDiscrepancyMapperTest {

    private ReconcileDiscrepancyMapper reconcileDiscrepancyMapper;

    @BeforeEach
    public void setUp() {
        reconcileDiscrepancyMapper = new ReconcileDiscrepancyMapperImpl();
    }
}
