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
public class CancelTransaction implements IActionTransaction {

    /**
     * if you add some status you must add the same status to description
     *
     * @return active statuses
     */
    @Override
    public List<TransactionStatus> getActiveStatuses() {
        return Arrays.asList(TransactionStatus.DEBITED,
                TransactionStatus.CANCEL_FAILED,
                TransactionStatus.FAILED,
                TransactionStatus.SUCCESS,
                TransactionStatus.PAYING);
    }

    @Override
    public Collection<I18nDTO> getTitle() {
        return Arrays.asList(i18n(Lang.UZ, "Bekor qilish"), i18n(Lang.RU, "Отмена"), i18n(Lang.EN, "Cancel"));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Collection<I18nDTO> getDescription() {
        return Arrays.asList(
                i18n(Lang.EN, "This action active for DEBITED, CANCEL FAILED, FAIL, SUCCESS, PAYING statuses."),
                i18n(Lang.RU, "Это действие активно для статусов Дебетован, Отмена не прошла, отказано, Оплата не прошла, Идёт оплата."),
                i18n(Lang.UZ, "Bu amaliyot Дебетован, Отмена не прошла, отказано, Оплата не прошла, Идёт оплата statuslari uchun faol.")

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
                i18n(Lang.EN, "Are you sure cancel this transaction?"),
                i18n(Lang.UZ, "Siz haqiqatdan ham transaksiyani qaytarmoqchimisiz?"),
                i18n(Lang.RU, "Вы действительно хотите отменить транзакцию?")
        );
    }

    @Override
    public String getUri() {
        return "/api/overpay/cancel_gateway";
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
