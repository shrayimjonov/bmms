package uz.paykap.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.client.HttpServerErrorException;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.humo.service.result.HumoResult;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.base.SimpleResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.config.SvgateResult;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.util.Objects;

@UtilityClass
public class ExceptionUtil {

    public static boolean isTimeoutException(ResultData<?> resultData) {
        return resultData.getException() != null && Objects.equals(resultData.getException().getDetail(), ProblemEnum.TIMEOUT.getDetail());
    }

    public static boolean isTimeoutException(HasData<?> resultData, PayType payType) {
        if (payType == PayType.UZCARD) {
            return Objects.equals(resultData.getCode(), SvgateResult.TIMEOUT.getCode());
        } else if (payType == PayType.HUMO) {
            return Objects.equals(resultData.getCode(), HumoResult.TIMEOUT.getCode());
        }
        return Objects.equals(resultData.getCode(), BmResultStatus.TIMEOUT.getCode());
    }

    // todo - fix
    public static boolean isTimeoutException(SimpleResultData<?> simpleResultData) {
        return simpleResultData.getException() != null && simpleResultData.getException() instanceof HttpServerErrorException;
    }

    public static boolean isTimeoutException(CodedException codedException) {
        return Objects.equals(codedException.getDetail(), ProblemEnum.TIMEOUT.getDetail());
    }

}
