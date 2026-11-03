package uz.paykap.service.dto.result;

import uz.javlon.commons.result.HasResultCode;
import uz.javlon.commons.result.HasResultMessage;
import uz.javlon.commons.result.HasSuccess;

public interface HasCodeAndMessage extends HasResultCode, HasResultMessage, HasSuccess {

}
