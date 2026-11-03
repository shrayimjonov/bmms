package uz.paykap.service.abs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.abs.request.*;
import uz.paykap.service.abs.response.*;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service(AbsSimulationService.BEAN_NAME)
@ConditionalOnProperty(prefix = "application.abs", name = "serviceBeanName", havingValue = AbsSimulationService.BEAN_NAME)
public class AbsSimulationService extends AbstractAbsService {

    public static final String BEAN_NAME = "absSimulationService";
    public static final String ERROR_TEST_CASE = "ERROR_RESULT";

    private final BillRepository billRepository;

    public AbsSimulationService(BillRepository billRepository) {
        this.billRepository = billRepository;
        log.debug("ABS Simulation : ON");
    }

    public BmResultData<AbsTransactionResult> doTransaction(AbsTransactionRequest billed) {
        return new BmResultData<>(getTransactionResult(billed.getRequestId()));
    }

    @Override
    public BmResultData<AnorPayTransactionResult> doAnorPayTransaction(AnorPayTransactionRequest requisitesRequest) {
        if (Objects.equals(requisitesRequest.getPurposeId(), "55555")) {
            return new BmResultData<>(new AnorPayTransactionResult("12345", "5", "IN PROGRESS"));
        } else if (Objects.equals(requisitesRequest.getPurposeId(), "52525")) {
            return new BmResultData<>(new AnorPayTransactionResult("123452", "52", "REJECTED"));
        } else if (Objects.equals(requisitesRequest.getPurposeId(), "50505")) {
            return new BmResultData<>(new AnorPayTransactionResult("123450", "50", "SUCCESS"));
        }
        return new BmResultData<>(new AnorPayTransactionResult("12345", "5", "IN PROGRESS"));
    }

    private AbsTransactionResult getTransactionResult(String testCase) {
        if (Objects.equals(testCase, ERROR_TEST_CASE)) {
            return new AbsTransactionResult("0", "-4", "error", testCase);
        }
        var result = new AbsTransactionResult("123", "0", "SUCCESS", "");
        result.setDetails(new HashMap<>());
        return result;
    }

    public BmResultData<AbsAccountResult> sendGetAccountRequest(Long requestId, String account, String mfo) {
        if (Objects.equals(account, "22222222222222222222")) {
            return new BmResultData<>(new AbsAccountResult()
                    .withName("Test Account")
                    .withSOutTmp("10000000")
                    .withTransId(account)
                    .withClient("Ivan Ivanov")
            );
        }
        return new BmResultData<>(new AbsAccountResult().withName("Test Account Name").withTransId(account));
    }

    public BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId) {
        return new BmResultData<>(getTransactionResult(requestId));
    }

    @Override
    public BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId, NciUpdateTranGroupType groupType) {
        return new BmResultData<>(getTransactionResult(requestId));
    }

    @Override
    public BmResultData<AbsTransactionResult> doForexTransaction(AbsForexTransactionRequest transactionDTO) {
        return new BmResultData<>(getTransactionResult(transactionDTO.getRequestId()));
    }

    @Override
    public BmResultData<List<AbsClientResult>> getClient(AbsClientRequest clientDTO) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<List<AbsBlackListResult>> getBlackList(AbsBlackListRequest nciHelperBlackListRequest) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<List<AbsCoursesResult>> getCourses(AbsCoursesRequest request) {
        AbsCoursesResult rub = new AbsCoursesResult();
        rub.setErrorCode("0");
        rub.setErrorNote("SUCCESS");
        rub.setTransId("5");
        rub.setBaseCurCode("000");
        rub.setBaseCurIsoCode("UZS");
        rub.setBranchExtid("00491");
        rub.setCurCode("643");
        rub.setCurIsoCode("RUB");
        rub.setEndDate("2021-10-14 19:40:04.0");
        rub.setRateBuy("185");
        rub.setRateDate("2021-05-25 00:00:00.0");
        rub.setRateSell("110");
        rub.setScale("1");
        rub.setNameCurrency("Российский рубль");

        AbsCoursesResult usd = new AbsCoursesResult();
        usd.setErrorCode("0");
        usd.setErrorNote("SUCCESS");
        usd.setTransId("5");
        usd.setBaseCurCode("000");
        usd.setBaseCurIsoCode("UZS");
        usd.setBranchExtid("00491");
        usd.setCurCode("840");
        usd.setCurIsoCode("USD");
        usd.setEndDate("2021-10-14 19:40:04.0");
        usd.setRateBuy("10740");
        usd.setRateDate("2021-09-30 00:00:00.0");
        usd.setRateSell("10660");
        usd.setScale("1");
        usd.setNameCurrency("Доллар США");

        AbsCoursesResult eur = new AbsCoursesResult();
        eur.setErrorCode("0");
        eur.setErrorNote("SUCCESS");
        eur.setTransId("5");
        eur.setBaseCurCode("000");
        eur.setBaseCurIsoCode("UZS");
        eur.setBranchExtid("00491");
        eur.setCurCode("978");
        eur.setCurIsoCode("EUR");
        eur.setEndDate("2021-10-14 19:40:04.0");
        eur.setRateBuy("13600");
        eur.setRateDate("2021-09-30 00:00:00.0");
        eur.setRateSell("11400");
        eur.setScale("1");
        eur.setNameCurrency("Евро");

        return new BmResultData<>(Arrays.asList(rub, usd, eur));
    }

    @Override
    public BmResultData<Bill> confirmAnorPayTransaction(Transaction transaction, AnorPayTransactionRequest mainRequest, AnorPayTransactionResult mainResult) {
        Bill bill = new Bill()
                .type(RequestType.CONFIRM)
                .amountRatio(transaction.getAmountRatio())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .login(transaction.getRequestLogin())
                .success(true)
                .requestJson(Collections.singletonMap("test", "test"))
                .transactionId(transaction.getId())
                .recipientShortName("ANOR-confirm-test")
                .amount(transaction.getSenderAmount())
                .account(transaction.getSenderAccount())
                .currency(transaction.getSenderCcy());
        ;

        return new BmResultData<>(billRepository.save(bill));
    }

    @Override
    public BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId, String eposMerchant,NciUpdateTranGroupType tranGroupType) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<List<MultiTransactionResultDTO>> doMultiTransaction(AbsMultiTransactionDTO transactionDTO) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<AbsAccountResult> sendGetAccountRequest(AbsAccountDTO accountRequestDto) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }
}
