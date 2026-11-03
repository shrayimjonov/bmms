package uz.paykap.adapter.verification;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.ExcelFileHandler;
import uz.paykap.adapter.verification.handler.SystemTransactionsHandler;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;
import uz.paykap.utils.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;

public class FromExcelReconcileServiceV2 extends FromFileReconcileServiceV2 {

    public static final String XLSX = "xlsx";
    public static final String XLS = "xls";
    private final Logger log = LoggerFactory.getLogger(FromExcelReconcileServiceV2.class);
    private final ExcelFileHandler excelFileHandler;
    private final ReconcileDelegateService reconcileDelegateService;

    public FromExcelReconcileServiceV2(String gatewayName, ExcelFileHandler excelFileHandler, SystemTransactionsHandler systemTransactionsHandler, ReconcileDelegateService reconcileDelegateService) {
        super(gatewayName, systemTransactionsHandler, reconcileDelegateService);
        this.excelFileHandler = excelFileHandler;
        this.reconcileDelegateService = reconcileDelegateService;
    }

    @Override
    ResultData<ReconcileGatewayDTO> readFromFile(String gateway, Instant fromTime, Instant toTime) {

        boolean isXlsxFile;

        String filePathToUpload = PathUtils.getFilePathToUpload(gateway, fromTime, reconcileDelegateService.getProperties(), XLSX);

        File excelFile = new File(filePathToUpload);

        if (!excelFile.exists() || excelFile.isDirectory()) {

            excelFile = new File(PathUtils.getFilePathToUpload(gateway, fromTime, reconcileDelegateService.getProperties(), XLS));

            if (!excelFile.exists() || excelFile.isDirectory()) {
                return new ResultData<>(new CodedException(ProblemEnum.OBJECT_NOT_FOUND, filePathToUpload));
            }

            //Found xls file
            isXlsxFile = false;
        } else {
            //Found xlsx file
            isXlsxFile = true;
        }

        return readExcel(excelFile, isXlsxFile);
    }

    ResultData<ReconcileGatewayDTO> readExcel(File excelFile, boolean isXlsxFile) {

        try {
            FileInputStream fis = new FileInputStream(excelFile);
            // we create an Workbook object for our Excel File
            Workbook workbook;
            if (isXlsxFile) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }
            // we get first sheet
            Sheet sheet = workbook.getSheetAt(0);
            // we iterate on rows

            ReconcileGatewayDTO reconcileGatewayDTO = excelFileHandler.getReconcileGatewayDTO(sheet);

            log.info("\nExcel report file parsed :\nfile : {}\ntotalAmount : {} tiyin\ntotalCount : {}\n", excelFile.getAbsolutePath(), reconcileGatewayDTO.getTotalAmountTiyin(), reconcileGatewayDTO.getTotalCount());

            workbook.close();
            fis.close();

            return new ResultData<>(reconcileGatewayDTO);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultData<>(new CodedException(ProblemEnum.PARSE_ERROR, e.getLocalizedMessage()));
        }
    }

}
