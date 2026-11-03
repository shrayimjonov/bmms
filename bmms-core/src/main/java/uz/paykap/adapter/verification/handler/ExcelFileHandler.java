package uz.paykap.adapter.verification.handler;

import org.apache.poi.ss.usermodel.Sheet;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;

public interface ExcelFileHandler {
    ReconcileGatewayDTO getReconcileGatewayDTO(Sheet sheet);
}
