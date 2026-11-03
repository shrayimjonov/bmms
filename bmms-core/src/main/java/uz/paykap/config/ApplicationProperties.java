package uz.paykap.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.pusher.dto.PushType;
import uz.javlon.svgate.domain.Bank;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.VirtualUzcardReplenishDto;
import uz.paykap.utils.pc.PcRegex;

import java.io.File;
import java.util.*;

/**
 * Properties specific to Pkbillms.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String name = "BillMS";
    private Boolean disableSslVerification = true;
    private Boolean simulateGatewayRequests = true;
    private Boolean simulateSendFileToEmail = true; //true bo'lsa MailService pochtaga xat jo'natadi
    private Boolean simulateBeelineRequests = true;
    private Boolean simulateVaqfRequests = true;
    private Boolean simulateUpayRequests = true;
    private Boolean simulateWebsumRequests = true;
    private Boolean simulateBankcreditRequests = true;
    private Boolean simulateUniversalbankRequests = true;
    private Boolean simulateCallbackRequests = true;
    private Boolean simulateBookkeepingRequests = true;
    private Boolean simulateUmsRequests = true;
    private Boolean simulateBillmasterRequests = true;
    private Boolean checkEnabled = true;
    private Boolean payEnabled = true;

    @Getter
    @Setter
    private Boolean allowOnPreHandle = true;

    @Getter
    @Setter
    private Boolean fraudEnabled = true;

    private String defaultEmailFrom = "user@example.com";
    private String defaultEmailTo = "user@example.com";
    private String defaultEmailFromKapital = "user@example.com";
    private String defaultEmailToKapital = "user@example.com";
    private String smsHost = "https://internal.example.com:17617/SMS.svc/send";
    private String smsKey = "";
    private String prefixFileName = "Kapital3";

    @Getter
    @Setter
    private String nciCourseId;

    @Getter
    @Setter
    private String uaaUserDefaultPassword = "12345";

    private Long smsSendingFrequency = 300L;

    @Getter
    @Setter
    private List<String> ourMfoPattern;

    @Getter
    @Setter
    private String defaultMfo = "00491";

    @Getter
    @Setter
    private String fileStorageBasePath = "." + File.separator + "uploads";

    private final Map<String, String> uzcardTokenMap = new HashMap<>(0);

    @Getter
    @Setter
    private Map<String, List<Bank>> pcCreditSettings;

    @Getter
    @Setter
    private Map<String, String> legalNameMap = new HashMap<>(0);

    @Getter
    @Setter
    private String defaultP2pLegalName = "Bank";

    @Getter
    @Setter
    private Integer currencyUz;

    @Getter
    @Setter
    private Boolean p2pCommissionFreeEnabled = false;

    private String humoGroupC = "01";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHumoGroupC() {
        return humoGroupC;
    }

    public void setHumoGroupC(String humoGroupC) {
        this.humoGroupC = humoGroupC;
    }

    public Boolean getCheckEnabled() {
        return checkEnabled;
    }

    public void setCheckEnabled(Boolean checkEnabled) {
        this.checkEnabled = checkEnabled;
    }

    public Boolean getPayEnabled() {
        return payEnabled;
    }

    public void setPayEnabled(Boolean payEnabled) {
        this.payEnabled = payEnabled;
    }

    public Long getSmsSendingFrequency() {
        return smsSendingFrequency;
    }

    public void setSmsSendingFrequency(Long smsSendingFrequency) {
        this.smsSendingFrequency = smsSendingFrequency;
    }

    public Boolean getSimulateCallbackRequests() {
        return simulateCallbackRequests;
    }

    public void setSimulateCallbackRequests(Boolean simulateCallbackRequests) {
        this.simulateCallbackRequests = simulateCallbackRequests;
    }

    public Boolean getSimulateUniversalbankRequests() {
        return simulateUniversalbankRequests;
    }

    public void setSimulateUniversalbankRequests(Boolean simulateUniversalbankRequests) {
        this.simulateUniversalbankRequests = simulateUniversalbankRequests;
    }

    public String getPrefixFileName() {
        return prefixFileName;
    }

    public void setPrefixFileName(String prefixFileName) {
        this.prefixFileName = prefixFileName;
    }

    private List<String> defaultToMails = Collections.singletonList("user@example.com");

    public List<String> getDefaultToMails() {
        return defaultToMails;
    }

    public void setDefaultToMails(List<String> defaultToMails) {
        this.defaultToMails = defaultToMails;
    }

    public String getSmsHost() {
        return smsHost;
    }

    public void setSmsHost(String smsHost) {
        this.smsHost = smsHost;
    }

    public String getSmsKey() {
        return smsKey;
    }

    public void setSmsKey(String smsKey) {
        this.smsKey = smsKey;
    }

    public String getDefaultEmailFromKapital() {
        return defaultEmailFromKapital;
    }

    public void setDefaultEmailFromKapital(String defaultEmailFromKapital) {
        this.defaultEmailFromKapital = defaultEmailFromKapital;
    }

    public String getDefaultEmailToKapital() {
        return defaultEmailToKapital;
    }

    public void setDefaultEmailToKapital(String defaultEmailToKapital) {
        this.defaultEmailToKapital = defaultEmailToKapital;
    }

    private Integer multiStatusListSize = 10;
    private Boolean activeCheckRequestVerifier = false; //true bo'lsa AutomaticCheckRequestSender hamma gateway bo'yicha gateway.check() o'tkazadi
    private final ApplicationProperties.Reconciliation reconciliation = new Reconciliation();
    private final ApplicationProperties.Paynet paynet = new ApplicationProperties.Paynet();
    private final ApplicationProperties.Common common = new ApplicationProperties.Common();
    private final ApplicationProperties.Otp otp = new ApplicationProperties.Otp();
    private final ApplicationProperties.Sms sms = new ApplicationProperties.Sms();
    private final ApplicationProperties.Pusher pusher = new ApplicationProperties.Pusher();
    private final ApplicationProperties.PaynetWsdl paynetWsdl = new ApplicationProperties.PaynetWsdl();
    private final ApplicationProperties.Deposit deposit = new ApplicationProperties.Deposit();
    private final ApplicationProperties.BookkeepingTransaction bookkeepingTransaction = new ApplicationProperties.BookkeepingTransaction();
    private final ApplicationProperties.Repay repay = new ApplicationProperties.Repay();
    private final ApplicationProperties.TransactionVerifier transactionVerifier = new ApplicationProperties.TransactionVerifier();
    private final ApplicationProperties.Payms payms = new ApplicationProperties.Payms();
    private final ApplicationProperties.VirtualAccount virtualAccount = new ApplicationProperties.VirtualAccount();
    private final ApplicationProperties.MunisAgent munisAgent = new ApplicationProperties.MunisAgent();
    private final ApplicationProperties.Humo humo = new ApplicationProperties.Humo();
    @Getter
    private final ApplicationProperties.HumoDebitGateway humoDebitGateway = new ApplicationProperties.HumoDebitGateway();
    @Getter
    private final ApplicationProperties.HumoCreditGateway humoCreditGateway = new ApplicationProperties.HumoCreditGateway();
    private final ApplicationProperties.TransactionPrefix transactionPrefix = new ApplicationProperties.TransactionPrefix();

    @Getter
    private final ApplicationProperties.AgentAccountPayment agentPayment = new ApplicationProperties.AgentAccountPayment();

    @Getter
    private final ApplicationProperties.Ofd ofd = new ApplicationProperties.Ofd();

    @Getter
    private final Abs abs = new Abs();

    @Getter
    private final Hold hold = new Hold();

    @Getter
    private final ApplicationProperties.P2pUzcard p2pUzcard = new ApplicationProperties.P2pUzcard();

    @Getter
    private final ApplicationProperties.P2p p2p = new ApplicationProperties.P2p();

    private final ApplicationProperties.Mail mail = new ApplicationProperties.Mail();

    @Getter
    private final CurrencyEventSenderProperties currencyEventSenderProperties = new CurrencyEventSenderProperties();

    public VirtualAccount getVirtualAccount() {
        return virtualAccount;
    }

    public Payms getPayms() {
        return payms;
    }

    public Boolean getSimulateVaqfRequests() {
        return simulateVaqfRequests;
    }

    public void setSimulateVaqfRequests(Boolean simulateVaqfRequests) {
        this.simulateVaqfRequests = simulateVaqfRequests;
    }

    public Boolean getSimulateUpayRequests() {
        return simulateUpayRequests;
    }

    public void setSimulateUpayRequests(Boolean simulateUpayRequests) {
        this.simulateUpayRequests = simulateUpayRequests;
    }

    public Boolean getSimulateBankcreditRequests() {
        return simulateBankcreditRequests;
    }

    public void setSimulateBankcreditRequests(Boolean simulateBankcreditRequests) {
        this.simulateBankcreditRequests = simulateBankcreditRequests;
    }

    public Boolean getSimulateWebsumRequests() {
        return simulateWebsumRequests;
    }

    public void setSimulateWebsumRequests(Boolean simulateWebsumRequests) {
        this.simulateWebsumRequests = simulateWebsumRequests;
    }

    public Map<String, String> getUzcardTokenMap() {
        return uzcardTokenMap;
    }

    public Mail getMail() {
        return mail;
    }

    public MunisAgent getMunisAgent() {
        return munisAgent;
    }

    public static class Deposit {

        private Boolean createIfNotExists = true;

        public Boolean getCreateIfNotExists() {
            return createIfNotExists;
        }

        public void setCreateIfNotExists(Boolean createIfNotExists) {
            this.createIfNotExists = createIfNotExists;
        }
    }

    /* Ignore hasCheck logic for gateways in the list in GatewayServiceV3 */
    @Getter
    @Setter
    private List<String> ignoreSkipCheckGateways = new ArrayList<>();

    @Getter
    @Setter
    public static class P2pUzcard {

        @Getter
        private final Map<P2pProcessorType, VirtualUzcardReplenishDto> virtualCardMap = new HashMap<>(5);

        private Map<P2pProcessorType, String> virtualCardTokens = new HashMap<>(5);

        private Long replenishFixedDelayMilliseconds;
        private Long replenishInitialDelayMilliseconds;
    }

    @Getter
    @Setter
    public static class P2p {

        private boolean getSenderInfoEnabled = true;
    }

    @Getter
    @Setter
    public static class Abs {

        private String mfo = "00981";
        private String getAccountSessionId = "1DEF345";
        private String serviceBeanName = "absSimulationService";
        private String nciTranStateId = "0";
    }

    @Getter
    @Setter
    public static class Humo {

        private String virtualPointCode = "100001104110";
        private String groupC = "01";
        private Map<String, String> additionalCentreIds = new HashMap<>();
        private Boolean verifyEnoughBalance = Boolean.FALSE;
        private Boolean verifyValidCard = Boolean.FALSE;
    }

    @Getter
    @Setter
    public static class AgentAccountPayment {

        private String defaultAccountPattern = "20208\\d{15}";
    }

    @Data
    public static class HumoDebitGateway {

        private String virtualPointCode = "100001104110";
        private String groupC = "01";
        private String panPattern = "9860\\d{12}";
        private Long eposMerchantId;
        private Long eposTerminalId;
    }

    @Data
    public static class HumoCreditGateway {

        private String virtualPointCode = "100001104110";
        private String groupC = "01";
        private String panPattern = "9860\\d{12}";
        private Long eposMerchantId;
        private Long eposTerminalId;
    }
    @Data
    public static class Common {

        private Boolean proxyEnabled = false;
        private String proxyHost = "127.0.0.1";
        private Integer proxyPort = 3737;
        private ArrayList<String> includedRecipients;
        private Boolean enabledRestTemplateLogging = true;
        private Integer defaultReadTimeout = 30000;
        private Integer defaultConnectTimeout = 3000;
    }

    @Data
    public static class Paynet {

        private Boolean fromFile = true;
        private Boolean downloadImages = false;
        private String paynetJsonPath = "." + File.separator + "paynet.json";
        private String uploadDir = "." + File.separator + "uploads";
        private String baseUrl = "https://smart.paynet.uz";
        private Boolean permitLogos = false;
    }

    @Data
    public static class PaynetWsdl {

        private String username = "";
        private String password = "";
    }

    @Data
    public static class Ofd {

        private Boolean enabled = false;
        private String url = "";
        private String tokenUrl = "";
        private String username = "";
        private String password = "";
        private String clientId = "";
        private String clientSecret = "";
        private String grantType = "";
        private String scope = "";
        private List<String> supportedGatewayNames = Collections.singletonList("paynetJson3-gateway");
        private String paymentTemplateName = "paynet";
        private String p2pTemplateName = "p2p";
    }

    @Data
    public static class Otp {

        private Boolean simulation = true;
        private String defaultSimulationCode = "123321";
        private Integer entryLimit = 3;
        private Integer timeoutSeconds = 120;
        private String otpTemplate = "${otp}";
    }

    @Data
    public static class Payms {

        private Boolean enabled = false;
    }

    @Data
    public static class VirtualAccount {

        private Boolean createIsNotExist = false;
    }

    @Data
    public static class Repay {

        private Integer daysBefore = 2;
        private boolean active = false;
        private String terminalLogin = "internal";
    }

    @Data
    public static class Sms {

        private Boolean simulation = true;
    }

    @Data
    public static class Pusher {

        private Boolean enabled = false;
        private String host = "http://localhost:19991/api/push";
        private String username = "pkbillms";
        private String password = "";
    }

    @Data
    public static class Reconciliation {

        private boolean enableAuto = true;
        private String uploadedFolderPath = "." + File.separator + "uploaded-reconciliation";
        private String path = "." + File.separator + "reconciliation";
        private String humoTerminalCron = "0 0 3,8,15,20 * * ?";
        private boolean skipSaveZeroValues = true; // Skip saving if system.count == 0 and gateway.count == 0
    }


    @Data
    public static class BookkeepingTransaction {

        private String urlPay = "http://127.0.0.1:88/IBankService.asmx";
    }

    @Data
    public static class TransactionVerifier {

        private Long verificationTime = 300000L;
        private Integer yellowDangerCount = 1;
        private Integer orangeDangerCount = 3;
        private Integer redDangerCount = 5;
        private String recipients = "998901234567"; // Comma separated list of phones
        private String pusherTags = "bm-push,bm-verifier"; // Comma separated list of tags
        private PushType pushType = PushType.PHONE;

        private String messagePrefix = "\uD83C\uDD98 Кол-во ошибочных транзакций превышено : "; // Prefix for message
        private Boolean active = false; // true - enabled, false - disabled
        private Long minTotalCount = 2L; // Works only if transactions count more than [minTotalCount]
        private Integer limitPercent = 50; // Notify if errors more than [limitPercent] percent

        private List<TransactionStatus> errorStatuses = Arrays.asList(
                TransactionStatus.FAILED,
                TransactionStatus.CHECK_FAIL,
                TransactionStatus.NEW
        );

        private String failedByGatewayCron = "0 */15 * * * *";
        private String failedCountCron = "0 */5 0 * * *";
    }

    @Data
    public static class CurrencyEventSenderProperties {

        private Boolean currencySenderSimulate;
        private String url;
        private String username;
        private String password;
    }

    public BookkeepingTransaction getBookkeepingTransaction() {
        return bookkeepingTransaction;
    }

    public Boolean getDisableSslVerification() {
        return disableSslVerification;
    }

    public void setDisableSslVerification(Boolean disableSslVerification) {
        this.disableSslVerification = disableSslVerification;
    }

    public Boolean getSimulateGatewayRequests() {
        return simulateGatewayRequests;
    }

    public void setSimulateGatewayRequests(Boolean simulateGatewayRequests) {
        this.simulateGatewayRequests = simulateGatewayRequests;
    }

    public Boolean getSimulateSendFileToEmail() {
        return simulateSendFileToEmail;
    }

    public void setSimulateSendFileToEmail(Boolean simulateSendFileToEmail) {
        this.simulateSendFileToEmail = simulateSendFileToEmail;
    }

    public String getDefaultEmailFrom() {
        return defaultEmailFrom;
    }

    public void setDefaultEmailFrom(String defaultEmailFrom) {
        this.defaultEmailFrom = defaultEmailFrom;
    }

    public String getDefaultEmailTo() {
        return defaultEmailTo;
    }

    public void setDefaultEmailTo(String defaultEmailTo) {
        this.defaultEmailTo = defaultEmailTo;
    }

    public Integer getMultiStatusListSize() {
        return multiStatusListSize;
    }

    public void setMultiStatusListSize(Integer multiStatusListSize) {
        this.multiStatusListSize = multiStatusListSize;
    }

    public Boolean getActiveCheckRequestVerifier() {
        return activeCheckRequestVerifier;
    }

    public void setActiveCheckRequestVerifier(Boolean activeCheckRequestVerifier) {
        this.activeCheckRequestVerifier = activeCheckRequestVerifier;
    }

    public Repay getRepay() {
        return repay;
    }

    public PaynetWsdl getPaynetWsdl() {
        return paynetWsdl;
    }

    public Common getCommon() {
        return common;
    }

    public Paynet getPaynet() {
        return paynet;
    }

    public Otp getOtp() {
        return otp;
    }

    public Sms getSms() {
        return sms;
    }

    public Deposit getDeposit() {
        return deposit;
    }

    public Pusher getPusher() {
        return pusher;
    }

    public Reconciliation getReconciliation() {
        return reconciliation;
    }

   public TransactionVerifier getTransactionVerifier() {
        return transactionVerifier;
    }

    public Humo getHumo() {
        return humo;
    }

    @Data
    public static class Mail {

        private String from = "user@example.com";
    }

    public Boolean getSimulateBookkeepingRequests() {
        return simulateBookkeepingRequests;
    }

    public void setSimulateBookkeepingRequests(Boolean simulateBookkeepingRequests) {
        this.simulateBookkeepingRequests = simulateBookkeepingRequests;
    }

    public Boolean getSimulateUmsRequests() {
        return simulateUmsRequests;
    }

    public void setSimulateUmsRequests(Boolean simulateUmsRequests) {
        this.simulateUmsRequests = simulateUmsRequests;
    }

    public Boolean getSimulateBeelineRequests() {
        return simulateBeelineRequests;
    }

    public void setSimulateBeelineRequests(Boolean simulateBeelineRequests) {
        this.simulateBeelineRequests = simulateBeelineRequests;
    }

    public Boolean getSimulateBillmasterRequests() {
        return simulateBillmasterRequests;
    }

    public void setSimulateBillmasterRequests(Boolean simulateBillmasterRequests) {
        this.simulateBillmasterRequests = simulateBillmasterRequests;
    }

    public TransactionPrefix getTransactionPrefix() {
        return transactionPrefix;
    }

    @Getter
    @Setter
    public static class MunisAgent {

        private String anorRecipientIdInMunis = "063";
        private String munisAnorProviderId;
        private String anorBankMFo;
        private String bmMunisAgentUsername;
        private String bmMunisAgentPassword;
        private Boolean certificateServiceSimulate;
        private Boolean verifyCertificateDisabled = false;
    }

    @Getter
    @Setter
    private Map<P2pProcessorType, PcRegex> pcRegex;

    @Getter
    @Setter
    public static class TransactionPrefix {
        private String mainPrefix = "";
        private String p2pDebitPrefix = "BM_P2P_D_";
        private String p2pCreditPrefix = "BM_P2P_C_";
        private String p2pPrefix = "BM_P2P_";
        private String senderDebitPrefix = "BM_S_D_";
        private String senderCreditPrefix = "BM_S_C_";
        private String gatewayDebitPrefix = "BM_GW_D_";
        private String gatewayCreditPrefix = "BM_GW_C_";
        private String p2pCreditCommissionPrefix = "BM_P2P_COMM_C_";
        private String cancelPrefix = "BM_C_";
    }

    //    @Getter
    @Setter
    public static class Hold {

        private Integer maxReholdPercent;

        public Integer getMaxReholdPercent() {
            return maxReholdPercent == null || maxReholdPercent < 0 ? 0 : maxReholdPercent > 100 ? 100 : maxReholdPercent;
        }
    }
}
