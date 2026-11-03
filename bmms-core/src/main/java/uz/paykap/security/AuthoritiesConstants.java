package uz.paykap.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String REPLENISH = "ROLE_DEPOSIT_REPLENISH";

    public static final String VIEW_RECIPIENT_DEPOSIT = "ROLE_VIEW_RECIPIENT_DEPOSIT";

    public static final String AGENT = "ROLE_AGENT";
    public static final String SUB_AGENT = "ROLE_SUB_AGENT";
    public static final String ROLE_CAN_CANCEL = "ROLE_CAN_CANCEL";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String ROLE_VIEW_MACHINE = "ROLE_VIEW_MACHINE";
    public static final String ROLE_NEW_MACHINE = "ROLE_NEW_MACHINE";
    public static final String ROLE_NEW_MACHINE_GROUP = "ROLE_NEW_MACHINE_GROUP";
    public static final String ROLE_VIEW_TRAN_FILTER = "ROLE_VIEW_TRAN_FILTER";
    public static final String ROLE_KIOSK_PAYMENTS = "ROLE_KIOSK_PAYMENTS";
    public static final String ROLE_REPORT_VIEW = "ROLE_REPORT_VIEW";
    public static final String ROLE_ACCOUNTANT_VIEW = "ROLE_ACCOUNTANT_VIEW";
    public static final String ROLE_ACCOUNTANT_NEW_BK_TEMPLATE = "ROLE_ACCOUNTANT_NEW_BK_TEMPLATE";
    public static final String ROLE_ACCOUNTANT_EXTERNAL = "ROLE_ACCOUNTANT_EXTERNAL";
    public static final String ROLE_ACCOUNTANT_INTERNAL = "ROLE_ACCOUNTANT_INTERNAL";
    public static final String ROLE_VIEW_CHARTS = "ROLE_VIEW_CHARTS";
    public static final String ROLE_MACHINE_GROUP_BIND_TO_USER = "ROLE_MACHINE_GROUP_BIND_TO_USER";
    public static final String ROLE_PRIVILEGES_ACCESS_VIEW = "ROLE_PRIVILEGES_ACCESS_VIEW";
    public static final String ROLE_PRIVILEGES_ACCESS_NEW = "ROLE_PRIVILEGES_ACCESS_NEW";
    public static final String ROLE_PRIVILEGES_ACCESS_EDIT = "ROLE_PRIVILEGES_ACCESS_EDIT";
    public static final String ROLE_BIND_GATEWAY_PARAM = "ROLE_BIND_GATEWAY_PARAM";
    public static final String ROLE_DATA_ADAPTER = "ROLE_DATA_ADAPTER";
    public static final String ROLE_AGENT_EDITOR = "ROLE_AGENT_EDITOR";
    public static final String ROLE_AGENT_RECIPIENT = "ROLE_AGENT_RECIPIENT";
    public static final String ROLE_AGENT_RECIPIENT_EDITOR = "ROLE_AGENT_RECIPIENT_EDITOR";
    public static final String ROLE_BILL = "ROLE_BILL";
    public static final String ROLE_BILL_EDITOR = "ROLE_BILL_EDITOR";
    public static final String ROLE_CATEGORY = "ROLE_CATEGORY";
    public static final String ROLE_CATEGORY_EDITOR = "ROLE_CATEGORY_EDITOR";
    public static final String ROLE_DEPOSIT = "ROLE_DEPOSIT";
    public static final String ROLE_DEPOSIT_EDITOR = "ROLE_DEPOSIT_EDITOR";
    public static final String ROLE_DEPOSIT_REPLENISH = "ROLE_DEPOSIT_REPLENISH";
    public static final String ROLE_FIELD = "ROLE_FIELD";
    public static final String ROLE_FIELD_EDITOR = "ROLE_FIELD_EDITOR";
    public static final String ROLE_FIELD_VALUE = "ROLE_FIELD_VALUE";
    public static final String ROLE_FIELD_VALUE_EDITOR = "ROLE_FIELD_VALUE_EDITOR";
    public static final String ROLE_GATEWAY = "ROLE_GATEWAY";
    public static final String ROLE_GATEWAY_EDITOR = "ROLE_GATEWAY_EDITOR";
    public static final String ROLE_GATEWAY_PARAM = "ROLE_GATEWAY_PARAM";
    public static final String ROLE_GATEWAY_PARAM_EDITOR = "ROLE_GATEWAY_PARAM_EDITOR";
    public static final String ROLE_I18N = "ROLE_I18N";
    public static final String ROLE_I18N_EDITOR = "ROLE_I18N_EDITOR";
    public static final String ROLE_PREFIX = "ROLE_PREFIX";
    public static final String ROLE_PREFIX_EDITOR = "ROLE_PREFIX_EDITOR";
    public static final String ROLE_RECIPIENT = "ROLE_RECIPIENT";
    public static final String ROLE_RECIPIENT_EDITOR = "ROLE_RECIPIENT_EDITOR";
    public static final String ROLE_RECIPIENT_GATEWAY_ADD = "ROLE_RECIPIENT_GATEWAY_ADD";
    public static final String ROLE_RECIPIENT_GATEWAY_CHECK = "ROLE_RECIPIENT_GATEWAY_CHECK";
    public static final String ROLE_TRANSACTION = "ROLE_TRANSACTION";
    public static final String ROLE_TRANSACTION_EDITOR = "ROLE_TRANSACTION_EDITOR";
    public static final String ROLE_RECIPIENT_FILE_SCHEMA = "ROLE_RECIPIENT_FILE_SCHEMA";
    public static final String ROLE_RECIPIENT_FILE_SCHEMA_EDITOR = "ROLE_RECIPIENT_FILE_SCHEMA_EDITOR";
    public static final String ROLE_VIEW_RECIPIENT_DEPOSIT = "ROLE_VIEW_RECIPIENT_DEPOSIT";
    public static final String ROLE_VIEW_WIDGET = "ROLE_VIEW_WIDGET";
    public static final String ROLE_LIST_WIDGET = "ROLE_LIST_WIDGET";
    public static final String ROLE_BOOKKEEPING_TEMPLATE_EDITOR = "ROLE_BOOKKEEPING_TEMPLATE_EDITOR";
    public static final String ROLE_DEPOSIT_TRANSACTION = "ROLE_DEPOSIT_TRANSACTION";
    public static final String ROLE_SETTING = "ROLE_SETTING";
    public static final String ROLE_SETTING_EDITOR = "ROLE_SETTING_EDITOR";
    public static final String ROLE_SIMPLE_REPORT = "ROLE_SIMPLE_REPORT";
    public static final String ROLE_SIMPLE_REPORT_EDITOR = "ROLE_SIMPLE_REPORT_EDITOR";
    public static final String ROLE_RECONCILE_SUMMARY = "ROLE_RECONCILE_SUMMARY";
    public static final String ROLE_RECONCILE_SUMMARY_EDITOR = "ROLE_RECONCILE_SUMMARY_EDITOR";
    public static final String ROLE_TRANSACTION_INFO = "ROLE_TRANSACTION_INFO";
    public static final String ROLE_RECONCILE_SUMMARY_UPLOAD_FILE = "ROLE_RECONCILE_SUMMARY_UPLOAD_FILE";
    public static final String ROLE_CAN_LIST_LOGINS = "ROLE_CAN_LIST_LOGINS";
    public static final String ROLE_CAN_LIST_MACHINES = "ROLE_CAN_LIST_MACHINES";
    public static final String ROLE_CAN_LIST_GATEWAYS = "ROLE_CAN_LIST_GATEWAYS";
    public static final String ROLE_CAN_LIST_RECIPIENTS = "ROLE_CAN_LIST_RECIPIENTS";
    public static final String ROLE_HISTORY_TRUSTED = "ROLE_HISTORY_TRUSTED";
    public static final String ROLE_CAN_MATCH_CARD_ACC = "ROLE_CAN_MATCH_CARD_ACC";

    public static final String ROLE_BM_HOLD = "ROLE_BM_HOLD";
    public static final String ROLE_BM_OTP_HOLD = "ROLE_BM_OTP_HOLD";
    public static final String ROLE_BM_P2P = "ROLE_BM_P2P";
    public static final String ROLE_BM_PAY = "ROLE_BM_PAY";

    private AuthoritiesConstants() {
    }
}
