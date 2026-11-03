package uz.paykap.adapter.verification;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import uz.javlon.commons.base.ResultData;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;
import uz.paykap.gateway.paynet.PaynetExcelFileHandler;

import java.io.File;
import java.io.IOException;

@Ignore("Ignored because, PaynetGatewayService is removed from DI, use new GatewayServiceV3")
public class FromExcelReconcileServiceV2Test {

    @Test
    public void dummy() {

    }

    @Test
    public void readPaynetExcelOk() throws IOException {

        FromExcelReconcileServiceV2 fromFileReconcileServiceV2 = new FromExcelReconcileServiceV2("paynet-gateway", new PaynetExcelFileHandler(), null, null);

        File file = new ClassPathResource("test-data/report_10.11.2018.xls").getFile();

        ResultData<ReconcileGatewayDTO> reconcileGatewayDTOResultData = fromFileReconcileServiceV2.readExcel(file, false);

        Assertions.assertThat(reconcileGatewayDTOResultData).isNotNull();
        Assertions.assertThat(reconcileGatewayDTOResultData.getData()).isNotNull();
        Assertions.assertThat(reconcileGatewayDTOResultData.getData().getTotalAmountTiyin()).isEqualTo(1400000);
        Assertions.assertThat(reconcileGatewayDTOResultData.getData().getTotalCount()).isEqualTo(7);
        Assertions.assertThat(reconcileGatewayDTOResultData.getData().getGatewayTranDTOS()).isNotEmpty();
        Assertions.assertThat(reconcileGatewayDTOResultData.getData().getGatewayTranDTOS().size()).isEqualTo(7);
    }

    @Test
    public void readPaynetExcelFileNotFound() {

        FromExcelReconcileServiceV2 fromFileReconcileServiceV2 = new FromExcelReconcileServiceV2("paynet-gateway", new PaynetExcelFileHandler(), null, null);

        //Simulating not existing file
        ResultData<ReconcileGatewayDTO> reconcileGatewayDTOResultData = fromFileReconcileServiceV2.readExcel(new File("123123.xls"), false);

        Assertions.assertThat(reconcileGatewayDTOResultData).isNotNull();
        Assertions.assertThat(reconcileGatewayDTOResultData.getData()).isNull();
        Assertions.assertThat(reconcileGatewayDTOResultData.getException()).isNotNull();
    }
}
