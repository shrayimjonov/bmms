package uz.paykap.service;

import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.base.ResultData;
import uz.paykap.domain.Transaction;
import uz.paykap.service.dto.OverpayResult;
import uz.paykap.service.dto.PaymentEditDTO;
import uz.paykap.service.dto.TransactionDTO;

import java.time.Instant;
import java.util.List;

public interface OverpayService {

    /*
    Оплата с помощью логина и номера транзакции (лист) ,тех платежей , которые прошли чек запрос
    */
    ResultData<List<TransactionDTO>> payGateway(List<Long> tranId, String login);

    /*
    Отмена с помощью  номера транзакции ,тех платежей , которые прошли успешно оплачены (и на стороне поставщика , и на биллинге )
    */
    ResultData<OverpayResult> cancelGateway(Long tranId);

    /*
    Снятие с карты с помощью номера транзакции
    */
    ResultData<OverpayResult> payUzcard(Long tranId);

    /*
    Отмена (на стороне Узкард , реверсал)с помощью номера транзакции
    */
    ResultData<OverpayResult> cancelSender(Long tranId);

    /*
    Оплата с помощью логина и номера транзакции ,тех платежей , которые прошли чек запрос
    */
    TransactionDTO toPayGateway(Transaction transaction, String agentName);

    /*
    Отмена на стороне АБС , создание проводок для возврата , для дальнейшей обработки
    */
    ResultData<OverpayResult> cancelAbs(Long tranId);

//    ResultData<OverpayResult> pay(PaymentEditDTO paymentEditDTO);

    /*
    Редактирование платежа : поставщика , номера
    */
    TransactionDTO edit(PaymentEditDTO paymentEditDTO);

    /*
     * Оплата с помощью логина и номера транзакции,тех платежей,
     * которые прошли чек запрос но какимто способом не прошли оплату или вообше не оплачивали.
     * Вызвав, сначало проводит чек запрос, затем пай запрос (оплачивается заново).
     * */
    TransactionDTO createPay(CommonPayRequest payDTO);

    /*
    Возвращает лист транзакций , за определенный период времени
    */
    List<Transaction> getTransactionListByDate(Instant fromDate, Instant toDate);

    SimpleBillResponse doNewPayBill(Long tranId);

    /*
    Отправка данных по эмаил для отмены транзакции
    */
    boolean sendCancelData(Long tranId);

    /*
    Оплата , с помощью ввода суммы .
    */
    TransactionDTO enterAmount(PaymentEditDTO paymentEditDTO);

    /*
    Отмена с помощью номера транзакции в биллинге
    */
    TransactionDTO innerCancel(Long tranId);

    /*
     * Только проверяет статус трансакции в системе поставщика
     * */
    ResultData<OverpayResult> checkStatusFromGateway(Long transactionId);

    /*
     * Проверяет статус трансакции в системе поставщика и обновляет в нашей базе
     * */
    ResultData<OverpayResult> syncStatusFromGateway(Long transactionId);

}
