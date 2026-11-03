package uz.paykap.service.action.transaction;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.I18nDTO;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class CancelRecipientTransactionAction implements IActionTransaction {

    @Override
    public List<TransactionStatus> getActiveStatuses() {
        return Arrays.asList(
                TransactionStatus.PAYING,
                TransactionStatus.SUCCESS,
                TransactionStatus.FAILED,
                TransactionStatus.CANCELLED,
                TransactionStatus.REVERSED,
                TransactionStatus.CANCEL_FAILED,
                TransactionStatus.R_CANCEL_FAILED,
                TransactionStatus.S_CANCEL_FAILED,
                TransactionStatus.DEBITED,
                TransactionStatus.WAITING,
                TransactionStatus.RECIPIENT_CANCELLED,
                TransactionStatus.SENDER_CANCELLED,
                TransactionStatus.S_NEED_CONFIRM,
                TransactionStatus.R_NEED_CONFIRM
        );
    }

    @Override
    public Collection<I18nDTO> getTitle() {
        return Arrays.asList(
                i18n(Lang.UZ, "Qabul qiluvchi tranzaksiyani bekor qilish"),
                i18n(Lang.RU, "Отменить транзакцию получателя"),
                i18n(Lang.EN, "Cancel recipient transaction"));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Collection<I18nDTO> getDescription() {
        return Arrays.asList(
                i18n(Lang.EN, "This action active for PAYING SUCCESS FAILED CANCELLED REVERSED CANCEL_FAILED R_CANCEL_FAILED S_CANCEL_FAILED RECIPIENT_CANCELLED SENDER_CANCELLED S_NEED_CONFIRM R_NEED_CONFIRM statuses."),
                i18n(Lang.RU, "Это действие активно для статусов PAYING SUCCESS FAILED CANCELLED REVERSED CANCEL_FAILED R_CANCEL_FAILED S_CANCEL_FAILED RECIPIENT_CANCELLED SENDER_CANCELLED S_NEED_CONFIRM R_NEED_CONFIRM"),
                i18n(Lang.UZ, "Bu amaliyot PAYING SUCCESS FAILED CANCELLED REVERSED CANCEL_FAILED R_CANCEL_FAILED S_CANCEL_FAILED RECIPIENT_CANCELLED SENDER_CANCELLED S_NEED_CONFIRM R_NEED_CONFIRM statuslari uchun faol.")

        );
    }

    @Override
    public Boolean isActive() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    @Override
    public Boolean isConfirmable() {
        return true;
    }

    @Override
    public Collection<I18nDTO> getConfirmationText() {
        return Arrays.asList(
                i18n(Lang.EN, "Do you really want to cancel recipient the transaction?"),
                i18n(Lang.UZ, "Haqiqatan ham qabul qiluvchining tranzaksiyasini bekor qilmoqchimisiz?"),
                i18n(Lang.RU, "Вы действительно хотите отменить транзакцию получателя?")
        );
    }

    @Override
    public String getUri() {
        return "api/transaction/cancel/recipient";
    }

    @Override
    public Map<String, Object> getUriParams() {
        return null;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    @Override
    public Map<String, ?> getBtnParams() {
        return null;
    }

    @Override
    public String[] getPathVariables() {
        return new String[]{"id"};
    }

}
