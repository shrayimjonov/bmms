//package uz.paykap.gateway.uzcard;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import uz.devops.bmcore.domain.CommonBill;
//import uz.devops.bmcore.dto.request.CommonCancelRequest;
//import uz.devops.bmcore.dto.request.CommonCheckRequest;
//import uz.devops.bmcore.dto.request.CommonPayRequest;
//import uz.devops.bmcore.dto.request.CommonStatusRequest;
//import uz.devops.bmcore.dto.response.DepositBalanceResponse;
//import uz.devops.bmcore.dto.response.SimpleBillResponse;
//import uz.devops.bmcore.enumeration.RequestType;
//import uz.devops.bmcore.gateway.GatewayService;
////import uz.javlon.commons.base.ResultData;
//import uz.javlon.commons.exception.CodedException;
//import uz.javlon.commons.exception.ProblemEnum;
//import uz.javlon.commons.util.CardUtil;
//import uz.javlon.commons.util.CommissionUtil;
//import uz.javlon.svgate.config.SvgateProperties;
//import uz.javlon.svgate.dto.TranDTO;
//import uz.javlon.svgate.dto.sv.P2pInfoDTO;
//import uz.javlon.svgate.dto.sv.SvResponseWithData;
//import uz.javlon.svgate.service.SvGatePaymentServiceV2;
//
//import uz.paykap.config.ApplicationProperties;
//import uz.paykap.config.Constants;
//import uz.paykap.domain.Bill;
//import uz.paykap.domain.Recipient;
//import uz.paykap.security.SecurityUtils;
//import uz.paykap.service.RecipientService;
//import uz.paykap.utils.ExtIdGeneratorInterface;
//import uz.paykap.utils.UzcardExtIdGenerator;
//
//import java.time.Instant;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Objects;
//import java.util.regex.Pattern;
//
//@Deprecated
//@Service(UzcardGatewayService.NAME)
//public class UzcardGatewayService extends GatewayService {
//
//    private final Logger log = LoggerFactory.getLogger(UzcardGatewayService.class);
//
//    public static final String NAME = "uzcardGatewayService";
//    //    public static final String DEBIT_SHORT_NAME = "uzcard-debit";
//    public static final String CREDIT_SHORT_NAME = "uzcard-credit";
//    public static final String P2P_CREDIT_SHORT_NAME = "uzcard-p2p-credit";
////    public static final String P2P_DEBIT_SHORT_NAME = "uzcard-p2p-debit";
//
//    private final Pattern cardPanPattern;
//    private final UzcardSenderCardResolver uzcardSenderCardResolver;
//    private ExtIdGeneratorInterface extIdGenerator = new UzcardExtIdGenerator();
//
//    @Override
//    public SimpleBillResponse check(CommonCheckRequest checkDTO) {
//
//        log.info("Uzcard check : {}", checkDTO);
//
//        if (checkDTO.getAccount() == null || checkDTO.getAccount().isEmpty() || checkDTO.getAmount() == null || checkDTO.getAmount() < 1) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS);
//        }
//
//        if (!cardPanPattern.matcher(checkDTO.getAccount()).matches()) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS);
//        }
//
//        Recipient recipient = recipientService.getRecipientOrThrow(checkDTO.getRecipientId());
//
//        log.debug("Found Recipient : {}", recipient);
//
//        boolean isCredit = Objects.equals(CREDIT_SHORT_NAME, recipient.getShortName()) || Objects.equals(P2P_CREDIT_SHORT_NAME, recipient.getShortName());
//
//        if (isCredit) {
//            //Check if bank supported
//            getBank(checkDTO.getAccount());
//
//            log.debug("Bank is supported for : {}", CardUtil.mask(checkDTO.getAccount()));
//        }
//
//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//
//        CommonBill bill = new Bill()
//            .transactionId(checkDTO.getTranId())
//            .recipientId(checkDTO.getRecipientId())
//            .recipientShortName(recipient.getShortName())
//            .account(checkDTO.getAccount())
//            .amount(checkDTO.getAmount())
//            .amountRatio(1)
//            .currency(checkDTO.getCurrency() != null ? checkDTO.getCurrency() : 860)
//            .type(RequestType.CHECK)
//            .login(login)
//            .requestTime(Instant.now())
//            .responseTime(Instant.now())
//            //
//            ;
//
//        bill.setRequestJson(checkDTO.getParams());
//
//        bill = commonBillService.save(bill);
//
//        log.debug("Created check-bill for Uzcard p2p : {}", bill);
//
//        if (!isCredit) {
//            bill
//                .responseTime(Instant.now())
//                .responseText(Constants.OK)
//                .responseCode(Constants.OK)
//                .success(cardPanPattern.matcher(checkDTO.getAccount()).matches());
//
//            return commonBillService.saveToSimple(bill);
//        }
//
//        P2pInfoDTO p2pInfoDTO;
//
//        try {
//            p2pInfoDTO = uzcardService.p2pInfo(checkDTO.getAccount());
//            String masked = CardUtil.mask(p2pInfoDTO.getPan());
//
//            HashMap<String, Object> data = new HashMap<>();
//            data.put(Constants.PAN_PARAM, masked);
//            data.put("fio", p2pInfoDTO.getFio());
//            data.put("exists", String.valueOf(p2pInfoDTO.getFio() != null));
//
//            bill
//                .responseJson(data)
//                .responseTime(Instant.now())
//                .responseText(Constants.OK)
//                .responseCode(Constants.OK)
//                .success(true)
//            ;
//        } catch (Exception e) {
//            log.warn("Uzcard : {}", e.getLocalizedMessage());
//            bill
//                .responseJson(MapUtil.errorMap(e))
//                .responseTime(Instant.now())
//                .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
//                .responseCode("FAILED")
//                .success(false)
//            ;
//        }
//
//        return commonBillService.saveToSimple(bill);
//    }
//
//    @Override
//    public SimpleBillResponse pay(CommonPayRequest payDTO) {
//        //TODO - Javlon, fix gateway amount
//
//        log.info("Pay uzcard : {}", payDTO);
//
//        String login = SecurityUtils.getCurrentUserLogin().orElse("");
//
//        CommonBill checkBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(payDTO.getBillId(), true, login, RequestType.CHECK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        CommonBill bill = new Bill()
//            .transactionId(payDTO.getTranId())
//            .recipientId(checkBill.getRecipientId())
//            .checkId(checkBill.getCheckId())
//            .payId(checkBill.getPayId())
//            .recipientShortName(UzcardDebitGatewayServiceV3.NAME)
//            .account(checkBill.getAccount())
//            .amount(checkBill.getAmount())
//            .amountRatio(checkBill.getAmountRatio())
//            .currency(checkBill.getCurrency())
//            .type(RequestType.PAY)
//            .login(login)
//            .requestTime(Instant.now())
//            .responseTime(Instant.now())
//            //
//            ;
//
//        bill = commonBillService.save(bill);
//
//        log.info("Created pay-bill for Uzcard p2p : {}", bill);
//
//        try {
//            P2pInfoDTO p2pInfoDTO = uzcardService.p2pInfo(checkBill.getAccount());
//
//            log.info("Bank cardBin: {}", p2pInfoDTO.getPan().substring(0, 6));
//
////            Bank bank = bankRepository.findTop1ByCardBin(p2pInfoDTO.getPan().substring(0, 6)).filter(Bank::isp2p).orElseThrow(() -> new CodedException(ProblemEnum.BANK_NOT_SUPPORTED));
//            SvgateProperties.Bank bank = getBank(checkBill.getAccount());
//
////            if (!BankUtil.validMerchant(p2pInfoDTO.getPan(), bank.getMerchantId())) {
////                log.debug("P2P merchant not valid for this institution : {}, {}", p2pInfoDTO.getPan(), bank.getMerchantId());
////                throw new CodedException(ProblemEnum.INTERNAL_ERROR);
////            }
//
//            //5af53b7cca57d70e28fd951f,860051******7097,2003,505000,500000,90510000205,92901684,14084
//
////            TranDTO tranDTO = uzcardService.p2pPanToPan(
////                properties.getWm().getPan(),
////                checkBill.getAccount(),
////                properties.getWm().getExp(),
////                StringUtils.substring(p2pInfoDTO.getExp(), 2, 6),
////                CommissionUtil.calcDebitTiyin(checkBill.getAmount(), bank.getCommission()), checkBill.getAmount() * 100,
////                bank.getMerchantId(), bank.getTerminalId(), bank.getPort()
////            );
//
//            ResultData<String> resultData = uzcardSenderCardResolver.resolveSenderToken(checkBill.getAccount());
//
//            if (!resultData.isSuccess()) {
//                throw resultData.getException();
//            }
//
//            String cardToken = resultData.getData();
//
//            log.info("\nId2Pan : \n\twmCardToken : {}, \n\tpan : {}, \n\texpiry : {}, \n\tdebitAmount : {}, \n\tcreditAmount : {}, \n\tmerchant : {}, \n\tterminal : {}, \n\tport : {}\n",
//                cardToken,
//                checkBill.getAccount(),
//                StringUtils.substring(p2pInfoDTO.getExp(), 2, 6),
//                CommissionUtil.calcDebitTiyinFromTiyin(checkBill.getAmount(), bank.getCommission()),
//                checkBill.getAmount(),
//                bank.getMerchantId(),
//                bank.getTerminalId(),
//                bank.getPort());
//
//            // Uzcard-credit recipient tiyinda bo'lgani uchun va
//            // PayDto.gatewayAmount tiyinda double bo'lib kelgani uchun
//            // gatewayAmount.longValue() olinmoqda
//            if (payDTO.getGatewayAmount() == null) {
//                log.error("Uzcard Pay gateway amount is null");
//                throw new CodedException(ProblemEnum.INVALID_PARAMS, "gateway amount not found");
//            }
//            Long amount = payDTO.getGatewayAmount().longValue();
//
//            TranDTO tranDTO = uzcardService.p2pId2Pan(
//                cardToken,
//                checkBill.getAccount(),
//                StringUtils.substring(p2pInfoDTO.getExp(), 2, 6),
//                CommissionUtil.calcDebitTiyinFromTiyin(amount, bank.getCommission()),
//                amount,
//                bank.getMerchantId(),
//                bank.getTerminalId(),
//                bank.getPort(),
//                payDTO.getTranId() == null ? "5" + System.currentTimeMillis() : extIdGenerator.generate("5", payDTO.getTranId().toString())
//            );
//
//            String masked = CardUtil.mask(p2pInfoDTO.getPan());
//
//            HashMap<String, Object> data = new HashMap<>();
//            data.put(Constants.PAN_PARAM, masked);
//            data.put("fio", p2pInfoDTO.getFio());
//
//            UzcardUtils.setTranData(tranDTO, data);
//
//            bill
//                .checkId(String.valueOf(checkBill.getId()))
//                .responseJson(data)
//                .responseTime(Instant.now())
//                .responseText(StringUtils.substring(tranDTO.getRespDesc(), 0, 128))
//                .responseCode(String.valueOf(tranDTO.getResp()))
//                .extId(tranDTO.getRefNum())
//                .success(tranDTO.getResp() == 0)
//            ;
//        } catch (Exception e) {
//            bill
//                .responseJson(Collections.singletonMap("error", e.getLocalizedMessage()))
//                .responseTime(Instant.now())
//                .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
//                .responseCode("FAILED")
//                .success(false)
//            ;
//        }
//
//        return commonBillService.saveToSimple(bill);
//    }
//
//    @Override
//    public SimpleBillResponse status(CommonStatusRequest dto) {
//
//        log.info("Status uzcard : {}", dto);
//
//        if (dto.getBillId() == null) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS);
//        }
//
//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//
//        CommonBill bill = commonBillService.findTop1ByIdAndLoginAndType(dto.getBillId(), login, RequestType.PAY)
//            .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        if (true) {
//            return commonBillService.toSimple(bill);
//        }
//
//        //TODO - fix
//
//        List<TranDTO> svTranDTOS = uzcardService.statusBySvId(bill.getExtId());
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
//        return commonBillService.toSimple(bill);
//    }
//
//    @Override
//    public SimpleBillResponse cancel(CommonCancelRequest dto) {
//        if (dto.getBillId() == null) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS);
//        }
//
//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//
//        CommonBill payBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(dto.getBillId(), true, login, RequestType.PAY)
//            .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        CommonBill bill = new Bill()
//            .transactionId(dto.getTranId())
//            .recipientId(payBill.getRecipientId())
//            .checkId(payBill.getCheckId())
//            .payId(payBill.getPayId())
//            .recipientShortName(UzcardDebitGatewayServiceV3.NAME)
//            .account(payBill.getAccount())
//            .amount(payBill.getAmount())
//            .amountRatio(payBill.getAmountRatio())
//            .currency(payBill.getCurrency())
//            .type(RequestType.CANCEL)
//            .login(login)
//            .requestTime(Instant.now())
//            //
//            ;
//
//        try {
//            SvResponseWithData<List<TranDTO>> tranDTOResponse = uzcardService.reverse(bill.getAccount());
//
//            TranDTO tranDTO;
//
//            if (tranDTOResponse == null || !tranDTOResponse.isSuccess() || tranDTOResponse.getData() == null || tranDTOResponse.getData().isEmpty()) {
//                bill
//                    .responseJson(Collections.singletonMap("error", "cancel not success"))
//                    .responseTime(Instant.now())
//                    .responseText("cancel not success")
//                    .responseCode("FAILED")
//                    .success(false)
//                ;
//                return commonBillService.saveToSimple(bill);
//            }
//
//            // TODO - check, in svgate doc response is object, but in lib is list
//            tranDTO = tranDTOResponse.getData().get(0);
//
//            String masked = CardUtil.mask(tranDTO.getPan());
//
//            HashMap<String, Object> data = new HashMap<>();
//            data.put(Constants.PAN_PARAM, masked);
//            data.put(Constants.AMOUNT_PARAM, String.valueOf(tranDTO.getAmount()));
//
//            UzcardUtils.setTranData(tranDTO, data);
//
//            bill
//                .responseJson(data)
//                .responseTime(Instant.now())
//                .responseText(StringUtils.substring(tranDTO.getRespDesc(), 0, 128))
//                .responseCode(String.valueOf(tranDTO.getResp()))
//                .extId(tranDTO.getRefNum())
//                .success(tranDTO.getResp() == 0)
//            ;
//        } catch (Exception e) {
//            bill
//                .responseJson(Collections.singletonMap("error", e.getLocalizedMessage()))
//                .responseTime(Instant.now())
//                .responseText(e.getLocalizedMessage())
//                .responseCode("FAILED")
//                .success(false)
//            ;
//        }
//
//        return commonBillService.saveToSimple(bill);
//    }
//
//    private SvgateProperties.Bank getBank(String cardBinOrPan) {
//        if (cardBinOrPan == null || cardBinOrPan.isEmpty()) {
//            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED);
//        }
//
//        if (cardBinOrPan.length() > 6) {
//            cardBinOrPan = cardBinOrPan.substring(0, 6);
//        }
//
//        SvgateProperties.Bank bank = svgateProperties.getBanks().get(cardBinOrPan);
//
//        if (bank == null || !bank.getP2p() || bank.getMerchantId() == null || bank.getMerchantId().isEmpty() || bank.getTerminalId() == null || bank.getTerminalId().isEmpty() || bank.getPort() == null) {
//            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED);
//        }
//
//        return bank;
//    }
//
//    @Override
//    public DepositBalanceResponse deposit() {
//        return null;
//    }
//
//    private final SvGatePaymentServiceV2 uzcardService;
//    private final ApplicationProperties properties;
//    private final SvgateProperties svgateProperties;
//    private final RecipientService recipientService;
//
//    public UzcardGatewayService(UzcardSenderCardResolver uzcardSenderCardResolver, RecipientService recipientService, SvGatePaymentServiceV2 uzcardService, ApplicationProperties properties, SvgateProperties svgateProperties) {
//        this.uzcardSenderCardResolver = uzcardSenderCardResolver;
//        this.recipientService = recipientService;
//        this.uzcardService = uzcardService;
//        this.properties = properties;
//        this.svgateProperties = svgateProperties;
//
//        cardPanPattern = Pattern.compile(svgateProperties.getPanPattern());
//    }
//
////    public TransactionDTO p2p(TransactionDTO transaction) {
////
////        P2pInfoDTO p2pInfoDTO = p2pInfo(transaction.getRecipientAccount());
////
////        Bank bank = bankRepository.findTop1ByCardBin(p2pInfoDTO.getPan().substring(0, 6)).filter(Bank::isp2p).orElseThrow(() -> new CodedException(ProblemEnum.BANK_NOT_SUPPORTED));
////
////        FeeResult feeResult = feeService.calculateForRecipientAmount(transaction.getSenderCurrency(), transaction.getRecipientCurrency(), BigDecimal.valueOf(transaction.getRecipientAmount()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_CEILING));
////
////        Long creditAmount = transaction.getRecipientAmount().multiply((BigDecimal.valueOf(100).add(new BigDecimal(bank.getCommission())))).setScale(0, RoundingMode.DOWN).longValue();
////
////        TranDTO tranDTO = uzcardService.p2pPanToPan(
////            properties.getWm().getPan(),
////            transaction.getRecipientAccount(),
////            properties.getWm().getExp(),
////            p2pInfoDTO.getExp(),
////            transaction.getRecipientAmount().multiply(BigDecimal.valueOf(100).setScale(2, RoundingMode.DOWN)).longValue(),
////            creditAmount);
////
////        transaction.setRecipientStatus(String.valueOf(tranDTO.getResp()));
////        transaction.setRecipientStatusText(tranDTO.getStatus().name());
////        transaction.setRecipientTrId(tranDTO.getRefNum());
////        transaction.setRecipientDate(tranDTO.getDate7().toInstant());
////        transaction.setRecipientSuccess(tranDTO.getResp() == 0);
////
////        return transaction;
////    }
//
//    public enum UzcardAuthorizationType {
//        OFFLINE, ONLINE, SMS_2FA
//    }
//
//}
