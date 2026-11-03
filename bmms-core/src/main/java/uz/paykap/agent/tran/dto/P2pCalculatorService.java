package uz.paykap.agent.tran.dto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.Currency;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.p2p.calc.P2pCalcParticipantDto;
import uz.paykap.service.dto.p2p.calc.P2pCalcRequest;
import uz.paykap.service.dto.p2p.calc.P2pCalcResponse;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.response.P2pParticipantDto;
import uz.paykap.service.p2p.dto.response.P2pRateDto;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

import static uz.paykap.agent.tran.dto.CalculationService.decimal8Format;

@Slf4j
@Service
@AllArgsConstructor
public class P2pCalculatorService {

    private final RateService rateService;
    private final P2pCommissionService p2pCommissionService;
    private final P2pRequestScopedContext p2pRequestScopedContext;

    public void calc(P2pCalcRequest request, P2pCalcResponse response, P2pParamResult p2pParamResult) {

        if (request == null) {
            log.debug("Calc request is null");
            response
                    .withResult(BmResultStatus.INVALID_REQUEST);
            return;
        }

        if (request.getAmount() == null) {
            log.debug("request amount is null");
            response
                    .withResult(BmResultStatus.INVALID_REQUEST)
                    .withAgentTranId(request.getAgentTranId())
            ;
            return;
        }

        // Sender
        BigDecimal senderCommissionUp = p2pParamResult.getSenderCommissionUp();
        if (senderCommissionUp == null) {
            senderCommissionUp = BigDecimal.ZERO;
        }


        BigDecimal senderCommissionDown = p2pParamResult.getSenderCommissionDown();
        if (senderCommissionDown == null) {
            senderCommissionDown = BigDecimal.ZERO;
        }

        BigDecimal senderCommission = senderCommissionUp.add(senderCommissionDown);

        BigDecimal senderCommissionAmountInCents = BigDecimal.valueOf(request.getAmount()).multiply(senderCommission).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);


        // Recipient
        BigDecimal recipientCommissionUp = p2pParamResult.getRecipientCommissionUp();
        if (recipientCommissionUp == null) {
            recipientCommissionUp = BigDecimal.ZERO;
        }

        BigDecimal recipientCommissionDown = p2pParamResult.getRecipientCommissionDown();
        if (recipientCommissionDown == null) {
            recipientCommissionDown = BigDecimal.ZERO;
        }

        BigDecimal recipientCommission = recipientCommissionUp.add(recipientCommissionDown);

        BigDecimal recipientCommissionAmountInCents = BigDecimal.valueOf(request.getAmount()).multiply(recipientCommission).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);

        response
                .withSender(new P2pCalcParticipantDto(senderCommissionAmountInCents.longValue(), senderCommission, request.getFrom().getCurrency()))
                .withRecipient(new P2pCalcParticipantDto(recipientCommissionAmountInCents.longValue(), recipientCommission, request.getTo().getCurrency()))
                .withSuccess(true)
                .withResult(BmResultStatus.SUCCESS)
                .from(request)
        //
        ;
    }

    public void calcInfo(final P2pPayRequest request, final P2pResponse response, final P2pParamResult p2pParamResult, AbstractP2pSender p2pSender, AbstractP2pRecipient p2pRecipient) {

        if (p2pParamResult == null) {
            log.warn("P2pParam not found");
            return;
        }

        if (!(Objects.equals(request.getFrom().getCurrency(), Constants.UZS_CURRENCY) || Objects.equals(request.getTo().getCurrency(), Constants.UZS_CURRENCY)) && !request.getFrom().getCurrency().equals(request.getTo().getCurrency())) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "to.currency or recipient.currency must be 860 (UZS)");
        }

        boolean isUserBuyingCcy = Objects.equals(request.getFrom().getCurrency(), Constants.UZS_CURRENCY);

        BigDecimal recipientAmountInRecipientCcyInCents;
        BigDecimal senderAmountInSenderCcyInCents;

        Currency requestCcy = Currency.get(request.getCurrency());

        Currency fromCcy = Currency.get(request.getFrom().getCurrency());
        Currency toCcy = Currency.get(request.getTo().getCurrency());

        if (fromCcy == toCcy && toCcy == requestCcy) {
            log.debug("Same currencies used, just use amounts as is");
            response.withRate(new P2pRateDto()).withCurrency(request.getFrom().getCurrency());

            recipientAmountInRecipientCcyInCents = BigDecimal.valueOf(request.getAmount() != null ? request.getAmount() : 0);
            senderAmountInSenderCcyInCents = BigDecimal.valueOf(request.getAmount() != null ? request.getAmount() : 0);
        } else if (isUserBuyingCcy) {

            /* User is buying, Bank is selling */
            /* senderAmount ---- requestAmount ---> recipientAmount */
            /* UZS ---- ??? ---> USD */
            BigDecimal bankSellRate = rateService.sellRate(toCcy, p2pRecipient.getCurrencyGroup()).orElseThrow(() -> new CodedException(ProblemEnum.INTERNAL_ERROR, "Currency " + toCcy.name() + " not supported"));

            response.withRate(new P2pRateDto()
                    .withBankSell(bankSellRate.longValue())
                    .withBankBuy(rateService.buyRate(toCcy).orElse(BigDecimal.ZERO).longValue())
            );

            if (requestCcy == Currency.UZS) {
                /* UZS ---- UZS ---> USD */
                recipientAmountInRecipientCcyInCents = BigDecimal.valueOf(request.getAmount()).multiply(BigDecimal.valueOf(100)).divide(bankSellRate, 8, RoundingMode.DOWN);
                senderAmountInSenderCcyInCents = BigDecimal.valueOf(request.getAmount());

                if (!isWholeNumber(recipientAmountInRecipientCcyInCents)) {
                    recipientAmountInRecipientCcyInCents = recipientAmountInRecipientCcyInCents.setScale(0, RoundingMode.DOWN);

                    senderAmountInSenderCcyInCents = recipientAmountInRecipientCcyInCents.multiply(bankSellRate).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);//todo commissionFreeLimit bo'yicha so'rash kk
                }

            } else {
                /* UZS ---- USD ---> USD */
                recipientAmountInRecipientCcyInCents = BigDecimal.valueOf(request.getAmount()).setScale(0, RoundingMode.DOWN);
                senderAmountInSenderCcyInCents = BigDecimal.valueOf(request.getAmount()).multiply(bankSellRate).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            }


        } else {

            /* User is selling, Bank is buying */
            /* senderAmount ---- requestAmount ---> recipientAmount */
            /* USD ---- ??? ---> UZS */

            BigDecimal bankBuyRate = rateService.buyRate(fromCcy, p2pSender.getCurrencyGroup()).orElseThrow(() -> new CodedException(ProblemEnum.INTERNAL_ERROR, "Currency " + fromCcy.name() + " not supported"));

            response.withRate(new P2pRateDto()
                    .withBankSell(rateService.sellRate(fromCcy).orElse(BigDecimal.ZERO).longValue())
                    .withBankBuy(bankBuyRate.longValue())
            );

            if (requestCcy == Currency.UZS) {
                /* USD ---- UZS ---> UZS */
                recipientAmountInRecipientCcyInCents = BigDecimal.valueOf(request.getAmount()).setScale(8, RoundingMode.DOWN);
                senderAmountInSenderCcyInCents = BigDecimal.valueOf(request.getAmount()).divide(bankBuyRate, 8, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));

                if (!isWholeNumber(senderAmountInSenderCcyInCents)) {
                    senderAmountInSenderCcyInCents = senderAmountInSenderCcyInCents.setScale(0, RoundingMode.DOWN);
                    recipientAmountInRecipientCcyInCents = senderAmountInSenderCcyInCents.multiply(bankBuyRate).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
                }
            } else {
                /* USD ---- USD ---> UZS */
                recipientAmountInRecipientCcyInCents = BigDecimal.valueOf(request.getAmount()).multiply(bankBuyRate).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
                senderAmountInSenderCcyInCents = BigDecimal.valueOf(request.getAmount());
            }
        }


        if (response.getSender() != null) {
            // Sender
            p2pCommissionService.calcSenderCommission(response.getSender(), p2pParamResult.getSenderCommissionUp(), senderAmountInSenderCcyInCents);
        }

        if (response.getRecipient() != null) {
            // Recipient
            p2pCommissionService.calcRecipientCommission(response.getRecipient(), p2pParamResult.getRecipientCommissionDown(), recipientAmountInRecipientCcyInCents);
        }

    }

    private boolean isWholeNumber(BigDecimal number) {
        return number.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

//    public void calc(P2pResponse response, P2pDTO sender, P2pDTO recipient, Long requestAmount) {
//
//        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParamById(
//            sender.getCondition(),
//            recipient.getCondition()
//        );
//
//        if (!p2pParam.isPresent()) {
//            log.warn("P2pParam not found by : {}, {}", sender.getAccount(), recipient.getAccount());
//            return;
//        }
//
//        P2pParamResult p2pParamResult = p2pParam.get();
//
//        calc(response, p2pParamResult, requestAmount, request);
//    }

    public P2pResponse calcPay(final P2pPayRequest request, P2pParamResult p2pParamResult, CalcHandler calcHandler, AbstractP2pSender p2pSender, AbstractP2pRecipient p2pRecipient) {

        P2pResponse p2pResponse = new P2pResponse()
                .fromBase(request)
                .withSender(getSenderParticipantDto(request))
                .withRecipient(getRecipientParticipantDto(request));

        calcInfo(request, p2pResponse, p2pParamResult, p2pSender, p2pRecipient);

        Currency senderCcy = Currency.get(request.getFrom().getCurrency());
        Currency requestCcy = Currency.get(request.getCurrency());
        Currency recipientCcy = Currency.get(request.getTo().getCurrency());

        Long rate = Objects.equals(request.getFrom().getCurrency(), Constants.UZS_CURRENCY) ? p2pResponse.getRate().getBankSell() : p2pResponse.getRate().getBankBuy();

        log.info("\n\n====================== P2P Pay Calc ======================\n" +
                        "1.  Request amount.... : {} {} cents\n" +
                        "2.  Sender amount..... : {} {} cents\n" +
                        "3.  Recipient amount.. : {} {} cents\n" +
                        "4.  Sen commission.... : {} %\n" +
                        "5.  Rec commission.... : {} %\n" +
                        "6.  Used rate......... : {} cents\n" +
                        "7.  Sell rate......... : {} cents\n" +
                        "8.  Buy rate.......... : {} cents\n" +
                        "",

                decimal8Format.format(request.getAmount()), requestCcy,
                decimal8Format.format(p2pResponse.getSender().getAmount()), senderCcy,
                decimal8Format.format(p2pResponse.getRecipient().getAmount()), recipientCcy,
                decimal8Format.format(p2pParamResult.getSenderCommissionUp().add(p2pParamResult.getSenderCommissionDown())),
                decimal8Format.format(p2pParamResult.getRecipientCommissionUp().add(p2pParamResult.getRecipientCommissionDown())),

                rate,
                p2pResponse.getRate().getBankSell(),
                p2pResponse.getRate().getBankBuy()
        );

        calcHandler.setTransaction(rate.intValue(), p2pResponse.getSender().getAmount(), p2pResponse.getRecipient().getAmount(), p2pResponse.getSender().getCommissionAmount(), p2pResponse.getRecipient().getCommissionAmount(), 0d, 0L, 0d /*roundAmount.doubleValue()*/, Double.valueOf(p2pResponse.getRecipient().getAmount()))
        //
        ;
//        checkMobileCourseIsValid(request, p2pResponse);

        return p2pResponse;
    }

    protected P2pParticipantDto getSenderParticipantDto(P2pPayRequest p2PPayRequest) {

        P2pParticipantDto participantDto = p2pRequestScopedContext.getSenderParticipantDto();

        if (participantDto == null) {
            participantDto = new P2pParticipantDto(String.valueOf(p2PPayRequest.getFrom().getParams()), p2PPayRequest.getAmount(), 0L, p2PPayRequest.getFrom().getCurrency(), new HashMap<>());
        }

        return participantDto;
    }

    protected P2pParticipantDto getRecipientParticipantDto(P2pPayRequest p2PPayRequest) {

        P2pParticipantDto participantDto = p2pRequestScopedContext.getRecipientParticipantDto();

        if (participantDto == null) {
            participantDto = new P2pParticipantDto(String.valueOf(p2PPayRequest.getTo().getParams()), p2PPayRequest.getAmount(), 0L, p2PPayRequest.getTo().getCurrency(), new HashMap<>());
        }

        return participantDto;
    }

    public interface CalcHandler {
        void setTransaction(int ccyRate, long senderAmount, long recipientAmount, long systemCommissionUpAmount, long systemCommissionDownAmount, double systemFee, Long systemFeeAmount, Double roundAmount, Double gatewayAmount);
    }

    private Transaction setTransaction(Transaction transaction, BigDecimal roundAmount, BigDecimal gatewayAmount, Long systemFeeAmount, int ccyRate, long senderAmount, long recipientAmount, long systemCommissionUpAmount, long systemCommissionDownAmount, double systemFee) {
        return transaction
                .senderAmount(senderAmount)
                .ccyRate(ccyRate)
                .recipientAmount(recipientAmount)
                .gatewayAmount(gatewayAmount.doubleValue())
                .roundAmount(roundAmount.doubleValue())
                .systemCommissionUpAmount(systemCommissionUpAmount)
                .systemCommissionDownAmount(systemCommissionDownAmount)
                .systemCurrency(Constants.DEFAULT_CURRENCY_CODE)
                .systemFeeAmount(systemFeeAmount)
                .systemFee(systemFee)
                .agentFeeAmount(0L);
    }

}
