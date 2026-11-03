package uz.paykap.config;

import lombok.experimental.UtilityClass;
import uz.paykap.domain.enumeration.Currency;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Application constants.
 */
@UtilityClass
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String PAYNET_GLOBAL_SETTING_NAME = "paynet-global";
    public static final String WMAGENT_LOGIN = "wmagent";
    public static final String SYSTEM_ACCOUNT = "system";
    public static final Integer AMOUNT_RATIO_SUM = 1;
    public static final Integer AMOUNT_RATIO_TIYIN = 100;
    public static final Integer DEFAULT_AMOUNT_RATIO = AMOUNT_RATIO_TIYIN;
    public static final Integer UZS_CURRENCY = 860;
    //    public static final Integer RUB_CURRENCY = 643;
    public static final Integer USD_CURRENCY = 840;
    public static final Integer EUR_CURRENCY = 978;
    public static final String USD_CURRENCY_ALFA = "USD";
    public static final String EUR_CURRENCY_ALFA = "EUR";
    public static final String UZS_CURRENCY_ALFA = "UZS";
    public static final Integer RUR_CURRENCY = 643;
    public static final Integer DEFAULT_CURRENCY_CODE = UZS_CURRENCY;
    public static final Currency DEFAULT_CURRENCY = Currency.UZS;
    public static final Set<Integer> DEFAULT_CURRENCIES = new HashSet<>() {
        {
            add(DEFAULT_CURRENCY_CODE);
        }
    };
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "uz-lat";
    public static final String SYSTEM_AGENT_NAME = "system";
    public static final String ADMIN_USERNAME = "admin";
    public static final String INTERNAL_AGENT_NAME = "internal";
    public static final String SYSTEM_INN = "00000000";
    public static final DateTimeFormatter RECONCILE_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());
    public static final String OK = "OK";
    public static final String PAN_MATCHED_OK = "pan_matched_ok";
    public static final String DEPOSIT_UPDATE_ERROR = "DEPOSIT_UPDATE_ERROR";
    public static final String GIFT_AMOUNT_CENTS = "gift_amount";
    public static final String GIFT_AMOUNT_FOR_PAY = "gift_amount_for_pay";
    public static final String GIFT_CURRENCY = "gift_currency";

    public static final String PAN_PARAM = "pan";
    public static final String CHECK_REQUEST_PARAM = "check_request_id";
    public static final String MASKED_PAN_PARAM = "masked_pan";
    public static final String ACCOUNT_PARAM = "account";
    public static final String NAME_PARAM = "name";
    public static final String ACCOUNT_NAME_PARAM = "accountName";
    public static final String CLIENT_NUMBER_PARAM = "clientNumber";
    public static final String ACCOUNT_EXISTS_PARAM = "accountExists";
    public static final String MFO_PARAM = "mfo";
    public static final String AMOUNT_PARAM = "amount";
    public static final String FEE_AMOUNT_PARAM = "fee_amount";
    public static final String FIO_PARAM = "fio";
    public static final String PHONE_PARAM = "phone";
    public static final String CARD_PAN_PARAM = "card_pan";
    public static final String EXPIRY_PARAM = "exp";
    public static final String OTP_PARAM = "otp";
    public static final String CENTRE_ID_PARAM = "centre_id";
    public static final String CCY_PARAM = "ccy";
    public static final String POINT_CODE_PARAM = "point_code";
    public static final String ACTION_CODE_PARAM = "actionCode";
    public static final String REFERENCE_NUMBER_PARAM = "refNum";
    public static final String REF_NUM_PARAM = "REF_NUM";
    public static final String CARD_EXPIRY_PARAM = "card_exp";
    public static final String TO_ACCOUNT_PARAM = "to_account";
    public static final String TO_ACCOUNT_TYPE_PARAM = "to_account_type";
    public static final String FROM_ACCOUNT_PARAM = "from_account";
    public static final String FROM_ACCOUNT_TYPE_PARAM = "from_account_type";
    public static final String TOKEN_PARAM = "token";
    public static final String BANK_ACCOUNT_PARAM = "bank_account";
    public static final String UZCARD_BIN = "8600";
    public static final String BIN_PARAM = "bin";
    public static final String HUMO_BIN = "9860";
    public static final String VIRTUAL_CARD_TOKEN_PARAM = "v_token";
    public static final String EPOS_MERCHANT_PARAM = "epos_m";
    public static final String EPOS_TERMINAL_PARAM = "epos_t";
    public static final String UZCARD_CREDIT_TYPE_PARAM = "credit_type";
    public static final String SENDER_CARD = "sender_card";
    public static final String RECIPIENT_CARD = "recipient_card";
    public static final String HUMO_PSEUDO_PAN_PATTERN = "9860\\d{2}[A-Z]{6}\\d{4}";
    public static final String TAGS_PARAM = "tags";
    public static final String TAG_BM_PAYMENT = "bm-payment";
    public static final String TAG_BM_P2P = "bm-p2p";
    public static final String PARAM_SKIP_CHECK = "skipCheck";
    public static final String PARAM_SKIP_CANCEL = "skipCancel";
    public static final String NO_CHECK_ID_MESSAGE = "NO_CHECK_ID";
    public static final String SKIP_CHECK_IS_ON = "SKIP_CHECK_IS_ON";
    public static final String SKIP_PAY_IS_ON = "SKIP_PAY_IS_ON";
    public static final String SKIP_CANCEL_IS_ON = "SKIP_CANCEL_IS_ON";
    public static final String INVALID_PARAMS_TEXT = "invalid-params";
    public static final String NO_QUERY = "no-query";
    public static final String EMPTY_VALUE_TEXT = "empty-value";
    public static final String NOT_FOUND_TEXT = "not-found";
    public static final String THE_SYSTEM_IS_TEMPORARILY_UNAVAILABLE = "The system is temporarily unavailable";
    public static final long DEFAULT_AMOUNT = 0L;
    public static final String DEFAULT_SESSION_ID = "";
    public static final String DEFAULT_ACCOUNT = "";
    public static final String DEFAULT_BM_PREFIX = "bm";
    public static final String DEFAULT_BM_DELIMITER = "-";
    public static final String UZCARD_ACCOUNT_PREFIX = "8600";

    public static final String BTN_COLOR = "color";
    public static final String BTN_TEXT = "text";

    public static final String CHECK_STATUS_ID = "checkStatusId";
    public static final String AGENT_NAME = "AGENT_NAME";
    public static final String ID = "ID";
    public static final String REQUEST = "REQUEST";
    public static final String SHORT_NAME = "SHORT_NAME";
    public static final String AMOUNT = "AMOUNT";

    public static final String PAYMENT_REF = "PAYMENT_REF";
}
