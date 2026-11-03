package uz.paykap.service.action.job;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.config.Constants;
import uz.paykap.service.dto.I18nDTO;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReindexCache implements IJob {

    public final String name = this.getClass().getSimpleName();

    @Override
    public Collection<I18nDTO> getTitle() {
        return Arrays.asList(
                new I18nDTO(Lang.EN, "Clear cache"),
                new I18nDTO(Lang.RU, "Очистить Кэш"),
                new I18nDTO(Lang.UZ, "Keshni tozalash")
        );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<I18nDTO> getDescription() {
        return Arrays.asList(
                new I18nDTO(Lang.EN, "Clear cache"),
                new I18nDTO(Lang.RU, "Очистить Кэш"),
                new I18nDTO(Lang.UZ, "Keshni tozalash")
        );
    }

    @Override
    public Boolean isActive() {
        return true;
    }

    @Override
    public Boolean isConfirmable() {
        return true;
    }

    @Override
    public Collection<I18nDTO> getConfirmationText() {
        return Arrays.asList(
                new I18nDTO(Lang.EN, "Are you sure clear cache?"),
                new I18nDTO(Lang.RU, "Вы уверены, что хотите очистить кэш?"),
                new I18nDTO(Lang.UZ, "Haqiqatan ham keshni tozalamoqchimisiz?")
        );
    }

    @Override
    public String getUri() {
        return "api/reindex/caches";
    }

    @Override
    public Map<String, Object> getUriParams() {
        return new HashMap<>();
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    @Override
    public Map<String, ?> getBtnParams() {
        return new HashMap<>() {
            {
                put(
                        Constants.BTN_TEXT,
                        Arrays.asList(
                                new I18nDTO(Lang.EN, "Clear cache"),
                                new I18nDTO(Lang.RU, "Очистить Кэш"),
                                new I18nDTO(Lang.UZ, "Keshni tozalash")
                        )
                );
                put(Constants.BTN_COLOR, "primary");
            }
        };
    }
}
