package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.BaseCardDTO;
import uz.javlon.svgate.dto.rpc.request.OtpCardRequestDTO;
import uz.javlon.svgate.dto.rpc.request.OtpVerifyRequestDTO;
import uz.javlon.svgate.dto.rpc.response.OtpCardResponseDTO;
import uz.javlon.svgate.dto.rpc.response.OtpVerifyResponseDTO;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGateOtpCardService;
import uz.paykap.service.dto.SvCardDTO;
import uz.paykap.service.mapper.SvCardMapper;

import javax.validation.Valid;
import java.util.List;

import static uz.paykap.security.AuthoritiesConstants.ROLE_HISTORY_TRUSTED;

@Api(value = "Ресурс для работы с карт Uzcard")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/agent")
public class SharedUzCardResourceV3 {

    private final SvGateCardServiceV2 cardServiceV2;
    private final SvGateOtpCardService svGateOtpCardService;

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Создание карт Uzcard (V3)")
    @PostMapping("/uzcard/create")
    public ResponseEntity<CommonResultData<SvCardDTO>> createCard(@Valid @RequestBody BaseCardDTO baseCardDTO) {
        log.debug("REST request to createCard: {}", baseCardDTO);
        var cardData = cardServiceV2.createCard(baseCardDTO);
        log.debug("return result: {}", cardData);
        return ResponseEntity.ok(new CommonResultData<>(cardData, SvCardMapper.toSvCardV2DTO(cardData.getData()), cardData.getMessage()));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Получение карт Uzcard (V3)")
    @PostMapping("/uzcard/get")
    public ResponseEntity<HasData<List<SvCardV2DTO>>> getCards(@Valid @RequestBody List<String> tokens) {
        log.debug("REST request to getCards : {}", tokens);
        return ResponseEntity.ok(cardServiceV2.getCards(tokens));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Метод позволяет отправить OTP на номер телефона зарегистрированного на карту")
    @PostMapping("/uzcard/otp")
    public ResponseEntity<HasData<OtpCardResponseDTO>> cardOtp(@Valid @RequestBody OtpCardRequestDTO requestDTO) {
        log.debug("REST request to otp : {}", requestDTO);
        return ResponseEntity.ok(svGateOtpCardService.otpCard(requestDTO));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Метод для подтверждение OTP и получения реального токена. \nТокенизация предусматривает замену цифр карты, на \nкомбинацию цифр и букв, сгенерированных случайным \nобразом, которая используется для замены основного номера \nбанковской карты.")
    @PostMapping("/uzcard/otp/verify")
    public ResponseEntity<HasData<OtpVerifyResponseDTO>> cardOtpVerify(@Valid @RequestBody OtpVerifyRequestDTO requestDTO) {
        log.debug("REST request to Otp Verify : {}", requestDTO);
        return ResponseEntity.ok(svGateOtpCardService.otpVerifyCard(requestDTO));
    }

}
