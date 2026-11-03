package uz.paykap.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApiConstants {
    public static final String NOTE_UID = "Unique identifier - Unpredictable string - hash value used to share with partners. Can be a result of SHA1, MD5 or other hash generators";
    public static final String NOTE_CARD_PAN_MASKED = "[Masked] Primary Account Number (HPAN) or card number involved in the given transaction. For security reasons is masked.";
    public static final String NOTE_CARD_PAN_FULL = "Primary Account Number (HPAN) or card number involved in the given transaction. PAN has to represent a valid credit card number. Use Luhn algorithm implementation which aims to check for user mistake, not credit card validity!";
    public static final String NOTE_CARD_EXPIRY = "Expiration date of card. Format : yyMM";
    public static final String NOTE_STAN = "Field 11: Systems Trace Audit Number. Format : '\\d{6}'. A number assigned by a transaction originator to assist in identifying a transaction uniquely. The\n" +
        "trace number remains unchanged for all messages within the transaction. This field is also used to match a response message with its original message.";
    public static final String NOTE_DATE_F12 = "Field 12: Time, Local Transaction. Format : YYMMDDhhmmss. The local year, month, day, and time the transaction takes place at the card acceptor location in\n" +
        "authorization and financial messages. This field is also used to match a response message with its original message.";
    public static final String NOTE_DATE_F7 = "Field 7: Date and Time, Transmission. Format : MMDDhhmmss. Date and time this message was sent by the message initiator.";
    public static final String NOTE_REF_NUM = "A reference supplied by the system retaining the original source information and used to assist in\n" +
        "locating that information or a copy thereof. Format : an 12";
    public static final String EXAMPLE_REF_NUM = "003408457402";
    public static final String EXAMPLE_DATE_F7 = "0122155433";
    public static final String EXAMPLE_DATE_F12 = "170122155433";
    public static final String EXAMPLE_PAN_FULL_1 = "8600140000000000";
    public static final String EXAMPLE_PAN_MASKED_1 = "860014******1118";
    public static final String EXAMPLE_EXP_1 = "1610";
    public static final String EXAMPLE_PAN_FULL_2 = "8600140000000000";
    public static final String EXAMPLE_PAN_MASKED_2 = "860014******2227";
    public static final String EXAMPLE_EXP_2 = "1902";
    public static final String EXAMPLE_CARD_UID = "ca1d1111111111111111111111111111";
    public static final String EXAMPLE_MERCHANT_UID = "11111111111111111111111111111111";
    public static final String EXAMPLE_CARD_HOLDER_UID = "123456789qwertyasd123345";
    public static final String EXAMPLE_MERCHANT_ID = "000000123456789";
    public static final String EXAMPLE_TERMINAL_UID_1 = "11111111111111111111111111111111";
    public static final String EXAMPLE_TERMINAL_UID_2 = "22222222222222222222222222222222";
    public static final String NOTE_MERCHANT_UID = "Merchant's uid";
    public static final String NOTE_CARD_HOLDER_UID = "Card Holder's uid";
    public static final String EXAMPLE_MERCHANT_ACCOUNT_1 = "11111111111111111111";
    public static final String EXAMPLE_MERCHANT_ACCOUNT_2 = "22222222222222222222";
}
