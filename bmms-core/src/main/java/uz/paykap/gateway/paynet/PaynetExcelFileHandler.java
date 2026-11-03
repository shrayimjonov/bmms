package uz.paykap.gateway.paynet;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import uz.paykap.adapter.verification.handler.ExcelFileHandler;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;

import java.util.HashSet;
import java.util.Objects;

@Slf4j
public class PaynetExcelFileHandler implements ExcelFileHandler {

    private static DataFormatter dataFormatter = new DataFormatter();

    @Override
    public ReconcileGatewayDTO getReconcileGatewayDTO(Sheet sheet) {
        HashSet<GatewayTranDTO> tranDTOS = new HashSet<>();
        long totalAmountTiyin = 0L;
        Long totalCount = 0L;
        long skippedRows = 0L;

        int multiplier = 100; //need tiyin but amount is in sum in excel, put 1 if given tiyin, paynet amount value in sum.

        for (Row row : sheet) {

            if (row.getRowNum() == 0) {
                skippedRows++;
                continue; //just skip the rows if row number is 0, it is header
            }

            Double amount = row.getCell(7).getNumericCellValue(); // 7 - Сумма

            //Skip if amount = 0 and status != 'Успешно', because only payment has amount > 0
            if (amount == 0d || !Objects.equals(row.getCell(11).getStringCellValue(), "Успешно")) {
                skippedRows++;
                continue;
            }

            String extId = dataFormatter.formatCellValue(row.getCell(5)); // 5 - Номер чека
            String account = row.getCell(6).getStringCellValue(); // 6 - Клиент

            tranDTOS.add(new GatewayTranDTO(amount, account, extId, true));

            totalCount++;
            totalAmountTiyin += amount.longValue() * multiplier;
        }

        log.info("\nPaynet report file parsed : skippedRows : {}", skippedRows);

        return new ReconcileGatewayDTO(tranDTOS, totalAmountTiyin, totalCount);
    }
}
