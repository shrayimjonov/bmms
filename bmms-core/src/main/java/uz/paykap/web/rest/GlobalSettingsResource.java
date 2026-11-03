package uz.paykap.web.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.dto.GlobalSettings;

@Slf4j
@AllArgsConstructor
@ApiIgnore
@RestController
@RequestMapping("/api/global-settings")
public class GlobalSettingsResource {

    private final ApplicationProperties applicationProperties;

    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "')")
    @PostMapping("/set")
    public ResultData<GlobalSettings> setGlobalSettings(@RequestBody GlobalSettings globalSettings) {
        log.info("Request to change global settings :{}", globalSettings);

        if (globalSettings == null) {
            return new ResultData<>(new CodedException(ProblemEnum.INVALID_PARAMS));
        }

        if (globalSettings.getEnableCheck() != null) {
            applicationProperties.setCheckEnabled(globalSettings.getEnableCheck());
        }
        if (globalSettings.getEnablePay() != null) {
            applicationProperties.setPayEnabled(globalSettings.getEnablePay());
        }
        if (globalSettings.getAllowOnPreHandle() != null) {
            applicationProperties.setAllowOnPreHandle(globalSettings.getAllowOnPreHandle());
        }

        log.debug("Global settings changed");

        return new ResultData<>(new GlobalSettings(applicationProperties.getCheckEnabled(), applicationProperties.getPayEnabled(), applicationProperties.getAllowOnPreHandle()));
    }

    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "')")
    @PostMapping("/get")
    public ResultData<GlobalSettings> setGlobalSettings() {

        log.debug("Request to get Global settings");

        return new ResultData<>(new GlobalSettings(applicationProperties.getCheckEnabled(), applicationProperties.getPayEnabled(), applicationProperties.getAllowOnPreHandle()));
    }

}
