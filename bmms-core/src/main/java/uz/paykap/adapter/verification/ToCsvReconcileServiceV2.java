package uz.paykap.adapter.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.javlon.commons.base.Result;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.CsvFileHandler;
import uz.paykap.adapter.verification.handler.RawSystemTransactionsHandler;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.event.dto.AttachmentReceiverType;
import uz.paykap.event.dto.ReconcileFileReceiverDTO;
import uz.paykap.event.dto.SendEmailWithAttachmentEvent;
import uz.paykap.utils.PathUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Set;

public class ToCsvReconcileServiceV2 extends ToFileReconcileServiceV2 {

    private final Logger log = LoggerFactory.getLogger(ToCsvReconcileServiceV2.class);
    private final CsvFileHandler csvFileHandler;
    private final ReconcileDelegateService reconcileDelegateService;


    public ToCsvReconcileServiceV2(String gatewayName, RawSystemTransactionsHandler systemTransactionsHandler, CsvFileHandler csvFileHandler, ReconcileDelegateService reconcileDelegateService, ApplicationProperties applicationProperties) {
        super(gatewayName, systemTransactionsHandler, reconcileDelegateService, applicationProperties);
        this.csvFileHandler = csvFileHandler;
        this.reconcileDelegateService = reconcileDelegateService;
    }

    protected String writeToFile(Set<Transaction> transactions, String gatewayName, String login, Instant fromTime, Instant toTime) {

        String folderPath = PathUtils.getResultFolderPath(gatewayName, reconcileDelegateService.getProperties());

//        String csvFile = folderPath + gatewayName + ".csv";
        String csvFile = PathUtils.getResultFilePath(gatewayName, login, fromTime, reconcileDelegateService.getProperties(), "csv");

        log.debug("INFO - Saving file into : {}", csvFile);

        try {
            File file = new File(csvFile);
            file.getParentFile().mkdirs();
            new File(folderPath).mkdir();

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(csvFile);
            writeFile(writer, transactions);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.warn("BAD - cant write file : {}", e.getLocalizedMessage());
            e.printStackTrace();
            return "";
        }

        return csvFile;
    }

    private void writeFile(Writer w, Set<Transaction> transactions) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Transaction transaction : transactions) {
            sb.append(csvFileHandler.getCsvLine(transaction));
            sb.append("\n");
        }
        w.append(sb.toString());
    }

    @Override
    Result sendToMail(String filePath, String gatewayName, Instant fromTime) {
        if (filePath.isEmpty()) {
            return Result.FAILED;
        }
        reconcileDelegateService.getPublisher().publishEvent(new SendEmailWithAttachmentEvent(this, "Oborot za " + getYesterdayDate() + ". Shlyuz " + gatewayName, new ReconcileFileReceiverDTO(gatewayName, filePath, AttachmentReceiverType.RECIPIENT)));
        return Result.SUCCESS;
    }

    // TODO - optimize
    private String getYesterdayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }
}
