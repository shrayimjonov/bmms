package uz.paykap.gateway.uzcard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.request.CommonCancelRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.response.DepositBalanceResponse;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayService;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.config.SvgateProperties;
import uz.javlon.svgate.config.SvgateResult;
import uz.javlon.svgate.dto.BaseCardDTO;
import uz.javlon.svgate.dto.DebitTranDTO;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.enumeration.CardStatus;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.dto.sv.SvResponseWithData;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.javlon.svgate.service.impl.DefaultStanSequence;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.RecipientService;
import uz.paykap.utils.MapUtil;
import uz.paykap.utils.NumberUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Lazy
@Slf4j
@AllArgsConstructor
@Service(UzcardDebitGatewayService.NAME)
public class UzcardDebitGatewayService extends GatewayService {

    public static final String NAME = "uzcardDebitGatewayService";
    public static final String DEBIT_SHORT_NAME = "uzcard-debit";
    public static final String P2P_DEBIT_SHORT_NAME = "uzcard-p2p-debit";
    public static final String OTP_PARAM_NAME = "otp";
    public static final String PHONE_PARAM_NAME = "phone";
    public static final String TOKEN_PARAM_NAME = "token";
    public static final String PAN_PARAM_NAME = "pan";
    public static final String EXP_PARAM_NAME = "exp";

    @Override
    public SimpleBillResponse check(CommonCheckRequest checkDTO) {

        log.info("Uzcard debit check : {}", checkDTO);

        if (checkDTO.getAccount() == null || checkDTO.getAccount().isEmpty() || checkDTO.getAmount() == null || checkDTO.getAmount() < 1) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "Invalid account or amount");
        }

        if (MapUtil.keyNotFoundOrEmpty(checkDTO.getParams(), Constants.EXPIRY_PARAM)) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, Constants.EXPIRY_PARAM);
        }

        if (!pcHelper.isValidPan(checkDTO.getAccount(), P2pProcessorType.UZCARD)) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "card pan");
        }

        final var validCardResponse = svGateHelperService.getSvCard(checkDTO.getAccount());

        if (validCardResponse == null || !validCardResponse.isSuccess() || validCardResponse.getData() == null || validCardResponse.getData().isEmpty()) {
            throw new CodedException(ProblemEnum.CARD_NOT_VALID);
        }

        SvCardV2DTO validCard = validCardResponse.getData().get(0);

        if (!validCard.getSms() || validCard.getStatus() != CardStatus.VALID) {
            throw new CodedException(ProblemEnum.CARD_NOT_VALID);
        }

        Recipient recipient = recipientService.getRecipientOrThrow(checkDTO.getRecipientId());

        log.debug("Found Recipient : {}", recipient);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        CommonBill bill = new Bill()
                .transactionId(checkDTO.getTranId())
                .recipientId(checkDTO.getRecipientId())
                .recipientShortName(recipient.getShortName())
                .account(checkDTO.getAccount())
                .amount(checkDTO.getAmount())
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                //TODO - fix
//            .amount(checkDTO.getTransaction().getSenderAmount()) // As it is in tiyin already
//            .amountRatio(100)
                .currency(checkDTO.getCurrency() != null ? checkDTO.getCurrency() : 860)
                .type(RequestType.CHECK)
                .login(login)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                //
                ;

        //TODO - test
//        String senderAmount = DECIMAL_FORMAT.format(checkDTO.getAmount() / 100);
        bill.setResponseJson(Collections.singletonMap("senderAmount", NumberUtil.formatCents(checkDTO.getAmount())));

        checkDTO.getParams().put(PHONE_PARAM_NAME, validCard.getPhone());
        checkDTO.getParams().put(TOKEN_PARAM_NAME, validCard.getToken());
        bill.setRequestJson(checkDTO.getParams());

        bill = commonBillService.save(bill);

        log.debug("Created check-bill for Uzcard debit : {}", bill);

        bill
                .responseTime(Instant.now())
                .responseText(Constants.OK)
                .responseCode(Constants.OK)
                .success(pcHelper.isValidPan(checkDTO.getAccount(), P2pProcessorType.UZCARD));

        //TODO - test
//        foreignPayService.sendVerification(validCard.getToken(), bill.getId(), validCard.getPhone());
        foreignPayService.sendVerification(validCard.getToken(), bill.getId(), validCard.getPhone(), checkDTO.getAmount());

        log.info("Sent SMS to {}", validCard.getPhone());

        return commonBillService.saveToSimple(bill);
    }

    @Override
    public SimpleBillResponse pay(CommonPayRequest payDTO) {
        //TODO - Javlon, fix gateway amount

        if (MapUtil.keyNotFoundOrEmpty(payDTO.getParams(), OTP_PARAM_NAME)) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        final CommonBill checkBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(payDTO.getBillId(), true, login, RequestType.CHECK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        final Result verified = foreignPayService.verify(login, payDTO.getBillId(), String.valueOf(payDTO.getParams().get(OTP_PARAM_NAME)));

        if (verified.isSuccess()) {
            return pay(payDTO, checkBill);
        }

        throw verified.getException();
    }

    private SimpleBillResponse pay(CommonPayRequest payDTO, final CommonBill checkBill) {

        log.debug("Uzcard debit pay : {}", payDTO);

        String cardToken = String.valueOf(checkBill.getRequestJson().get(TOKEN_PARAM_NAME));

        CommonBill bill = new Bill()
                .transactionId(payDTO.getTranId())
                .recipientId(checkBill.getRecipientId())
//                .checkId(checkBill.getCheckId())
//                .payId(checkBill.getPayId())
                .recipientShortName(UzcardDebitGatewayServiceV3.NAME)
                .account(checkBill.getAccount())
                .amount(checkBill.getAmount())
                .amountRatio(checkBill.getAmountRatio())
                .currency(checkBill.getCurrency())
                .type(RequestType.PAY)
                .login(checkBill.getLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                //
                ;

        bill = commonBillService.save(bill);

        log.debug("Debiting from card token : {}", cardToken);

//        try {
        // 1. Debit
        var tranDTO = uzcardService.debit(getDebitTranDTOFromBill(checkBill, cardToken));
        log.debug("Debit result : {}", tranDTO);

        if (!tranDTO.isSuccess()) {
            bill
                    .responseJson(Collections.singletonMap("error", tranDTO.getMessage()))
                    .responseTime(Instant.now())
                    .responseText(tranDTO.getMessage())
                    .responseCode("FAILED")
                    .success(false)
            ;
            if (Objects.equals(SvgateResult.TIMEOUT.getCode(), tranDTO.getCode())) {
                bill.setResponseText(tranDTO.getMessage());
                bill.setResponseCode(String.valueOf(tranDTO.getCode()));
                return commonBillService.saveToSimple(bill).status(TransactionStatus.WAITING);
            }

            return commonBillService.saveToSimple(bill);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, tranDTO.getData().getPan());
//            data.put("fio", p2pInfo.getFio());
        UzcardUtils.setTranData(tranDTO.getData(), data);

        bill
//                .checkId(String.valueOf(checkBill.getId()))
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(StringUtils.substring(tranDTO.getData().getRespDesc(), 0, 128))
                .responseCode(String.valueOf(tranDTO.getData().getResp()))
                .extId(tranDTO.getData().getRefNum())
                .success(tranDTO.getData().getResp() == 0)
        ;
//        } catch (Exception e) {

//        }

        return commonBillService.saveToSimple(bill);
    }

    private DebitTranDTO getDebitTranDTOFromBill(CommonBill billDTO, String cardToken) {
        DebitTranDTO tranDTO = new DebitTranDTO();
        tranDTO.setCardId(cardToken);
        tranDTO.setStan(stanSequence.getNextStan());
        tranDTO.setCurrencyCode("860");
        tranDTO.setAmount(billDTO.getAmount());
        tranDTO.setExt(String.valueOf(billDTO.getId()));
        return tranDTO;
    }

    @Override
    public SimpleBillResponse status(CommonStatusRequest dto) {

        log.info("Status uzcard : {}", dto);

        SimpleBillResponse bill = super.status(dto);

//        if (true) {
        return bill;
//        }

        //TODO - fix

//        List<SvTranDTO> svTranDTOS = uzcardService.statusBySvId(bill.getExtId());
//
//        if (svTranDTOS != null && svTranDTOS.size() > 0) {
//            boolean a = !Objects.equals(bill.getResponseText(), svTranDTOS.get(0).getStatus().name());
//            boolean b = !Objects.equals(bill.getResponseCode(), String.valueOf(svTranDTOS.get(0).getResp()));
//            if (a) {
//                bill.setResponseText(svTranDTOS.get(0).getStatus().name());
//            }
//
//            if (b) {
//                bill.setResponseCode(String.valueOf(svTranDTOS.get(0).getResp()));
//            }
//
//            if (a || b) {
//                bill.setSuccess(svTranDTOS.get(0).getResp() == 0);
//                bill = commonBillService.save(bill);
//            }
//        }
//
//        return billMapper.toSimple(bill);
    }

    @Override
    public SimpleBillResponse cancel(CommonCancelRequest dto) {
        if (dto.getBillId() == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        CommonBill payBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(dto.getBillId(), true, login, RequestType.PAY)
                .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        Bill bill = new Bill()
                .transactionId(dto.getTranId())
                .recipientId(payBill.getRecipientId())
//                .checkId(payBill.getCheckId())
//                .payId(payBill.getPayId())
                .recipientShortName(UzcardDebitGatewayServiceV3.NAME)
                .account(payBill.getAccount())
                .amount(payBill.getAmount())
                .amountRatio(payBill.getAmountRatio())
                .currency(payBill.getCurrency())
                .type(RequestType.CANCEL)
                .login(login)
                .requestTime(Instant.now())
                //
                ;

//        try {
        var tranDTOResponse = uzcardService.reverse(bill.getAccount());

        TranDTO tranDTO;

        if (tranDTOResponse == null || !tranDTOResponse.isSuccess() || tranDTOResponse.getData() == null || tranDTOResponse.getData().isEmpty()) {
            bill
                    .responseJson(Collections.singletonMap("error", tranDTOResponse != null ? tranDTOResponse.getMessage() : "cancel not success"))
                    .responseTime(Instant.now())
                    .responseText(tranDTOResponse != null ? tranDTOResponse.getMessage() : "cancel not success")
                    .responseCode("FAILED")
                    .success(false)
            ;
            return commonBillService.saveToSimple(bill);
        }

        // TODO - check, in svgate doc response is object, but in lib is list
        tranDTO = tranDTOResponse.getData().get(0);

        String masked = CardUtil.mask(tranDTO.getPan());

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, masked);
        data.put(Constants.AMOUNT_PARAM, String.valueOf(tranDTO.getAmount()));
//            data.put("refnum", tranDTO.getRefNum());
//            data.put("date", tranDTO.getDate7().toString());

        UzcardUtils.setTranData(tranDTO, data);

        bill
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(tranDTO.getStatus().name())
                .responseCode(String.valueOf(tranDTO.getResp()))
                .extId(tranDTO.getRefNum())
                .success(tranDTO.getResp() == 0)
        ;
//        } catch (Exception e) {

//        }

        return commonBillService.saveToSimple(bill);
    }

    @Override
    public DepositBalanceResponse deposit() {
        return null;
    }

    private final SvGatePaymentServiceV2 uzcardService;
    private final SvGateCardServiceV2 uzcardCardService;
    private final DefaultStanSequence stanSequence;
    private final RecipientService recipientService;
    private final ForeignPayService foreignPayService;
    private final PcHelper pcHelper;
    private final SvGateHelperService svGateHelperService;


}

