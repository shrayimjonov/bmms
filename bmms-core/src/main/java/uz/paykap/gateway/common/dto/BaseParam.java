package uz.paykap.gateway.common.dto;

public enum BaseParam {
    tranId, /* Ид. транзакции в системе */
    account, /* Счет пользователя (request.account) */
    amount, /* Сумма платежа (request.amount) */
    password, /* Пароль в системе партнёра (gatewayParam.password) */
    username, /* Логин в системе партнёра (gatewayParam.username) */
    token, /* Токен в системе партнёра (gatewayParam.token) */
    timestamp, /* Дата проведения в секундах с 1970-01-01T00:00:00Z (генерируется во время проведения платежа) */
    timeinstant, /* Дата проведения в формате yyyy-MM-ddTHH:mm:ssZ в UTC (генерируется во время проведения платежа) */
    timesoap, /* Дата проведения в формате yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX (генерируется во время проведения платежа) */
    extId, /* Ид. транзакции в системе партнёра (доступен при PAY) */
    dateTo, /* Дата начало (доступен при RECONCILIATION) */
    dateFrom, /* Дата окончания (доступен при RECONCILIATION) */
    date, /* Текушая дата (доступен при RECONCILIATION) */
    billId, /* Ид. запроса в системе */
    timeWithZone, /* Дата проведения в формате yyyy-MM-dd'T'HH:mm:ss.SSS+05:00 в часовом полюсе системы (генерируется во время проведения платежа) */
    payTime /* Дата платежа в системе партнёра (доступен при CANCEL) */
}
