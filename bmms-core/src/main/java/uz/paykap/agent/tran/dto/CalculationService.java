package uz.paykap.agent.tran.dto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.HasResult;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.Currency;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class CalculationService {

    public static final int SCALE_AFTER_COMMA = 8;

    public final static DecimalFormat decimal8Format = new DecimalFormat("0.0000000000");
    public final static DecimalFormat decimal2Format = new DecimalFormat("0.00");

    private final RateService rateService;

    public void calc(Transaction transaction, GatewayQueryResult gatewayQueryResult) {

        if (transaction == null || transaction.getRequestAmount() == null) {
            log.debug("transaction or request amount is null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        if (Objects.equals(transaction.getRequestCcy(), gatewayQueryResult.getRecipientCcy())) {
            calcSameCcy(transaction, gatewayQueryResult);
            return;
        }

        Currency requestCcy = Currency.get(transaction.getRequestCcy());
        Currency systemCcy = uz.paykap.config.Constants.DEFAULT_CURRENCY;
        Currency recipientCcy = Currency.get(gatewayQueryResult.getRecipientCcy());

        log.debug("Recipient ccy : {}, Request ccy {}, System ccy {}", recipientCcy, recipientCcy, systemCcy);

        BigDecimal requestCcyRate = rateService.convert(requestCcy, 100L, systemCcy);
        BigDecimal recipientCcyRate = rateService.convert(recipientCcy, 100L, systemCcy);

        log.debug("Request   : 100 {} cents = {} {} cents", requestCcy, requestCcyRate, systemCcy);
        log.debug("Recipient : 100 {} cents = {} {} cents", recipientCcy, recipientCcyRate, systemCcy);

        // AGENT

        // Agent fee in RequestCcy currency
        Double agentFeeAmount = gatewayQueryResult.getAgentFee() / 100d * transaction.getRequestAmount();

        // Commissions

        BigDecimal requestAmountInSystemCcyCents = rateService.convert(requestCcy, transaction.getRequestAmount(), systemCcy);

        BigDecimal systemCommissionDownAmountCents;
        BigDecimal systemCommissionUpAmountCents;
        BigDecimal senderAmountCents;

        if (transaction.getPayType() == PayType.CASH) {
            // if CASH, commUp=0, commDown = commDown + commUp;
            systemCommissionUpAmountCents = BigDecimal.ZERO;
            // Calculate percent of commUp amount from request amount and add to commDown amount ---> [commDown + (reqAmount * commUp / 100)], commUp -> [0,100]
            systemCommissionDownAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionDown()).add(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp()))).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Send only requested amount
            senderAmountCents = BigDecimal.valueOf(transaction.getRequestAmount());
        } else {
            // Calculate percent of commUp amount from request amount ---> [reqAmount * commUp / 100], commUp -> [0,100]
            systemCommissionUpAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp())).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Calculate percent of commDown amount from request amount ---> [reqAmount * commDown / 100], commDown -> [0,100]
            systemCommissionDownAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionDown())).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Send [reqAmount + commUp amount]
            senderAmountCents = BigDecimal.valueOf(transaction.getRequestAmount()).add(BigDecimal.valueOf(transaction.getRequestAmount()).multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp())).divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR));
        }


        // Add sender amount rounding
        BigDecimal roundAmount = rateService.convert(requestCcy, senderAmountCents.add(BigDecimal.valueOf(senderAmountCents.longValue()).negate()), systemCcy);

        log.debug("sender amount : {}", senderAmountCents);

        BigDecimal recipientAmountCentsInSystemCcyAfterCommissions = requestAmountInSystemCcyCents.add(systemCommissionDownAmountCents.negate());
        roundAmount = roundAmount.add(recipientAmountCentsInSystemCcyAfterCommissions.add(BigDecimal.valueOf(recipientAmountCentsInSystemCcyAfterCommissions.longValue()).negate()));

        //Recipient

        BigDecimal rawGatewayAmount = rateService.convert(systemCcy, recipientAmountCentsInSystemCcyAfterCommissions, recipientCcy).setScale(SCALE_AFTER_COMMA, RoundingMode.FLOOR);

        int scale = gatewayQueryResult.getRecipientAmountRatio() == 100 ? 0 : 2;

        BigDecimal gatewayAmount = rawGatewayAmount.multiply(BigDecimal.valueOf(gatewayQueryResult.getRecipientAmountRatio() / 100d)).setScale(scale, RoundingMode.FLOOR);

//        roundAmount = roundAmount.add(rawGatewayAmount.add(gatewayAmount)) ;

        //TODO - we loose cents, fix later
//        if (gatewayQueryResult.getRecipientAmountRatio() == 1) {
//            roundAmount += gatewayAmount.doubleValue() - gatewayAmount.longValue();
//        }

//        String currencyAlfaCode = Constants.getAlfaCode(gatewayQueryResult.getRecipientCcy());

        Long systemFeeAmount = recipientAmountCentsInSystemCcyAfterCommissions.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemFee())).divide(BigDecimal.valueOf(100d), SCALE_AFTER_COMMA, RoundingMode.HALF_UP).longValue();

        log.info("\n\n" +
                        "1.  Request amount.... : {} {} cents\n" +
                        "2.  Amount in sys ccy. : {} {} cents\n" +
                        "3.  Sys com U amount.. : {} {} cents <- [2] * {}%\n" +
                        "4.  Sys com D amount.. : {} {} cents <- [2] * {}%\n" +
                        "5.  Sys fee amount.... : {} {} cents <- ([2]-[3]) * {}%\n" +
                        "6.  Agent fee amount.. : {} {} cents <- [1] * {}%\n" +
                        "7.  Sender amount..... : {} {} cents <- [1] + {}%\n" +
                        "8.  Rec amount........ : {} {} cents <- [2] - [4]\n" +
                        "9.  Rec amount ratio.. : {} {}\n" +
                        "10. Rec currency...... : {} \n" +
                        "11. Rec rate.......... : {} cents\n" +
                        "12. Raw Gateway amount : {} {} cents\n" +
                        "13. Gateway amount.... : {} {}\n" +
                        "14. Round amount...... : {} {} cents\n" +
                        "15. Pay type.......... : {}\n" +
                        "16. Min-Max........... : {}-{}\n",
                transaction.getRequestAmount(), requestCcy,
                requestAmountInSystemCcyCents, systemCcy,
                systemCommissionUpAmountCents, systemCcy, gatewayQueryResult.getSystemCommissionUp(),
                systemCommissionDownAmountCents, systemCcy, gatewayQueryResult.getSystemCommissionDown(),
                systemFeeAmount, systemCcy, gatewayQueryResult.getSystemFee(),
                agentFeeAmount, requestCcy, gatewayQueryResult.getAgentFee(),
                senderAmountCents, requestCcy, gatewayQueryResult.getSystemCommissionUp(),
                recipientAmountCentsInSystemCcyAfterCommissions, recipientCcy,
                "1/" + gatewayQueryResult.getRecipientAmountRatio(), (gatewayQueryResult.getRecipientAmountRatio() == 100 ? "(cents)" : "(dollars)"),
                recipientCcy,
                recipientCcyRate, //TODO - add requestCcyRate into transaction
                rawGatewayAmount, recipientCcy,
                gatewayAmount, recipientCcy,
                decimal8Format.format(roundAmount), recipientCcy,
                transaction.getPayType(),
                gatewayQueryResult.getRecipientMinAmount(), gatewayQueryResult.getRecipientMaxAmount()
        );

//        log.debug("System fee amount : {}", systemFeeAmount);

        transaction
                .senderAmount(senderAmountCents.longValue())
                .ccyRate(recipientCcyRate.intValue())
                .eposMerchant(gatewayQueryResult.getEposMerchant())
                .eposTerminal(gatewayQueryResult.getEposTerminal())
                .recipientAmount(recipientAmountCentsInSystemCcyAfterCommissions.longValue())
                .gatewayAmount(gatewayAmount.doubleValue())
                .roundAmount(roundAmount.doubleValue())
                .systemCommissionUpAmount(systemCommissionUpAmountCents.longValue())
                .systemCommissionDownAmount(systemCommissionDownAmountCents.longValue())
                .systemCurrency(uz.paykap.config.Constants.DEFAULT_CURRENCY_CODE)
                .systemFeeAmount(systemFeeAmount)
                .systemFee(gatewayQueryResult.getSystemFee())
                .agentFeeAmount(agentFeeAmount.longValue())
        //
        ;
    }

    public void calcSameCcy(Transaction transaction, GatewayQueryResult gatewayQueryResult) {

        if (transaction == null || transaction.getRequestAmount() == null) {
            log.debug("transaction or request amount is null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        if (!Objects.equals(transaction.getRequestCcy(), gatewayQueryResult.getRecipientCcy())) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "should be same ccy");
        }

        Currency requestCcy = Currency.get(transaction.getRequestCcy());
//        Currency systemCcy = uz.paykap.config.Constants.DEFAULT_CURRENCY;
        Currency recipientCcy = Currency.get(gatewayQueryResult.getRecipientCcy());

        log.debug("Recipient ccy : {}, Request ccy {}, System ccy {}", recipientCcy, recipientCcy, Constants.DEFAULT_CURRENCY);


        // AGENT

        // Agent fee in RequestCcy currency
        Double agentFeeAmount = gatewayQueryResult.getAgentFee() / 100d * transaction.getRequestAmount();

        // Commissions

        BigDecimal requestAmountInSystemCcyCents = BigDecimal.valueOf(transaction.getRequestAmount());

        BigDecimal systemCommissionDownAmountCents;
        BigDecimal systemCommissionUpAmountCents;
        BigDecimal senderAmountCents;

        if (transaction.getPayType() == PayType.CASH) {
            // if CASH, commUp=0, commDown = commDown + commUp;
            systemCommissionUpAmountCents = BigDecimal.ZERO;
            // Calculate percent of commUp amount from request amount and add to commDown amount ---> [commDown + (reqAmount * commUp / 100)], commUp -> [0,100]
            systemCommissionDownAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionDown()).add(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp()))).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Send only requested amount
            senderAmountCents = BigDecimal.valueOf(transaction.getRequestAmount());
        } else {
            // Calculate percent of commUp amount from request amount ---> [reqAmount * commUp / 100], commUp -> [0,100]
            systemCommissionUpAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp())).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Calculate percent of commDown amount from request amount ---> [reqAmount * commDown / 100], commDown -> [0,100]
            systemCommissionDownAmountCents = requestAmountInSystemCcyCents.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionDown())).divide(BigDecimal.valueOf(100L), SCALE_AFTER_COMMA, RoundingMode.FLOOR);
            // Send [reqAmount + commUp amount]
            senderAmountCents = BigDecimal.valueOf(transaction.getRequestAmount()).add(BigDecimal.valueOf(transaction.getRequestAmount()).multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemCommissionUp())).divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR));
        }


        // Add sender amount rounding
        BigDecimal roundAmount = senderAmountCents.add(BigDecimal.valueOf(senderAmountCents.longValue()).negate());

        log.debug("sender amount : {}", senderAmountCents);

        BigDecimal recipientAmountCentsInSystemCcyAfterCommissions = requestAmountInSystemCcyCents.add(systemCommissionDownAmountCents.negate());
        roundAmount = roundAmount.add(recipientAmountCentsInSystemCcyAfterCommissions.add(BigDecimal.valueOf(recipientAmountCentsInSystemCcyAfterCommissions.longValue()).negate()));

        //Recipient

        BigDecimal rawGatewayAmount = recipientAmountCentsInSystemCcyAfterCommissions.setScale(SCALE_AFTER_COMMA, RoundingMode.FLOOR);

        int scale = gatewayQueryResult.getRecipientAmountRatio() == 100 ? 0 : 2;

        BigDecimal gatewayAmount = rawGatewayAmount.multiply(BigDecimal.valueOf(gatewayQueryResult.getRecipientAmountRatio() / 100d)).setScale(scale, RoundingMode.FLOOR);

//        roundAmount = roundAmount.add(rawGatewayAmount.add(gatewayAmount)) ;

        //TODO - we loose cents, fix later
//        if (gatewayQueryResult.getRecipientAmountRatio() == 1) {
//            roundAmount += gatewayAmount.doubleValue() - gatewayAmount.longValue();
//        }

//        String currencyAlfaCode = Constants.getAlfaCode(gatewayQueryResult.getRecipientCcy());

        Long systemFeeAmount = recipientAmountCentsInSystemCcyAfterCommissions.multiply(BigDecimal.valueOf(gatewayQueryResult.getSystemFee())).divide(BigDecimal.valueOf(100d), SCALE_AFTER_COMMA, RoundingMode.HALF_UP).longValue();

        log.info("\n\n" +
                        "1.  Request amount.... : {} {} cents\n" +
                        "2.  Amount in sys ccy. : {} {} cents\n" +
                        "3.  Sys com U amount.. : {} {} cents <- [2] * {}%\n" +
                        "4.  Sys com D amount.. : {} {} cents <- [2] * {}%\n" +
                        "5.  Sys fee amount.... : {} {} cents <- ([2]-[3]) * {}%\n" +
                        "6.  Agent fee amount.. : {} {} cents <- [1] * {}%\n" +
                        "7.  Sender amount..... : {} {} cents <- [1] + {}%\n" +
                        "8.  Rec amount........ : {} {} cents <- [2] - [4]\n" +
                        "9.  Rec amount ratio.. : {} {}\n" +
                        "10. Rec currency...... : {} \n" +
                        "11. Rec rate.......... : {} cents\n" +
                        "12. Raw Gateway amount : {} {} cents\n" +
                        "13. Gateway amount.... : {} {}\n" +
                        "14. Round amount...... : {} {} cents\n" +
                        "15. Pay type.......... : {}\n" +
                        "16. Min-Max........... : {}-{}\n",
                transaction.getRequestAmount(), requestCcy,
                requestAmountInSystemCcyCents, requestCcy,
                systemCommissionUpAmountCents, requestCcy, gatewayQueryResult.getSystemCommissionUp(),
                systemCommissionDownAmountCents, requestCcy, gatewayQueryResult.getSystemCommissionDown(),
                systemFeeAmount, requestCcy, gatewayQueryResult.getSystemFee(),
                agentFeeAmount, requestCcy, gatewayQueryResult.getAgentFee(),
                senderAmountCents, requestCcy, gatewayQueryResult.getSystemCommissionUp(),
                recipientAmountCentsInSystemCcyAfterCommissions, recipientCcy,
                "1/" + gatewayQueryResult.getRecipientAmountRatio(), (gatewayQueryResult.getRecipientAmountRatio() == 100 ? "(cents)" : "(dollars)"),
                recipientCcy,
                recipientCcy, //TODO - add requestCcyRate into transaction
                rawGatewayAmount, recipientCcy,
                gatewayAmount, recipientCcy,
                decimal8Format.format(roundAmount), recipientCcy,
                transaction.getPayType(),
                gatewayQueryResult.getRecipientMinAmount(), gatewayQueryResult.getRecipientMaxAmount()
        );

//        log.debug("System fee amount : {}", systemFeeAmount);

        transaction
                .senderAmount(senderAmountCents.longValue())
                .ccyRate(100)
                .eposMerchant(gatewayQueryResult.getEposMerchant())
                .eposTerminal(gatewayQueryResult.getEposTerminal())
                .recipientAmount(recipientAmountCentsInSystemCcyAfterCommissions.longValue())
                .gatewayAmount(gatewayAmount.doubleValue())
                .roundAmount(roundAmount.doubleValue())
                .systemCommissionUpAmount(systemCommissionUpAmountCents.longValue())
                .systemCommissionDownAmount(systemCommissionDownAmountCents.longValue())
                .systemCurrency(uz.paykap.config.Constants.DEFAULT_CURRENCY_CODE)
                .systemFeeAmount(systemFeeAmount)
                .systemFee(gatewayQueryResult.getSystemFee())
                .agentFeeAmount(agentFeeAmount.longValue())
        //
        ;
    }

    public HasResult validateAmount(Long requestAmountInCents, Integer requestCcy, GatewayQueryResult gatewayQueryResult) {
        return validateAmount(requestAmountInCents, requestCcy, Constants.DEFAULT_AMOUNT_RATIO, gatewayQueryResult);
    }

    public HasResult validateAmount(Long requestAmountInCents, Integer requestCcy, Integer requestAmountRatio, GatewayQueryResult gatewayQueryResult) {

        BigDecimal amountInCcyInCents = rateService.convert(requestCcy, requestAmountInCents, gatewayQueryResult.getRecipientCcy());

        log.debug("amountInCcyInCents : {}", amountInCcyInCents);

        BigDecimal amountInCcyInRecipientRatio;

        if (Objects.equals(gatewayQueryResult.getRecipientLimitRatio(), Constants.AMOUNT_RATIO_SUM)) {
            amountInCcyInRecipientRatio = rateService.convertCentToDollar(amountInCcyInCents);
        } else {
            amountInCcyInRecipientRatio = amountInCcyInCents;
        }

        log.debug("amountInCcyInRecipientRatio : {}", amountInCcyInRecipientRatio);

        if (BigDecimal.valueOf(gatewayQueryResult.getRecipientMinAmount()).compareTo(amountInCcyInRecipientRatio) > 0) {
            log.warn("Min limit exceeded : {} > {}", gatewayQueryResult.getRecipientMinAmount(), amountInCcyInRecipientRatio);
            return new CommonResult(BmResultStatus.MIN_LIMIT_EXCEEDED, "Min limit exceeded : " + gatewayQueryResult.getRecipientMinAmount() + " > " + amountInCcyInRecipientRatio + " request=" + requestAmountInCents);
        }

        log.debug("OK - Min limit not exceeded : {} > {}", amountInCcyInRecipientRatio, gatewayQueryResult.getRecipientMinAmount());

        if (BigDecimal.valueOf(gatewayQueryResult.getRecipientMaxAmount()).compareTo(amountInCcyInRecipientRatio) < 0) {
            log.warn("Max limit exceeded : {} < {}", gatewayQueryResult.getRecipientMaxAmount(), amountInCcyInRecipientRatio);
            return new CommonResult(BmResultStatus.MAX_LIMIT_EXCEEDED, "Max limit exceeded : " + gatewayQueryResult.getRecipientMaxAmount() + " < " + amountInCcyInRecipientRatio + " request=" + requestAmountInCents);
        }

        log.debug("OK - Max limit not exceeded : {} < {}", amountInCcyInRecipientRatio, gatewayQueryResult.getRecipientMaxAmount());

        return HasResult.SUCCESS;
    }


}

