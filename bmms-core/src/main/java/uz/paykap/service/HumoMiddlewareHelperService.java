package uz.paykap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.humoMiddleware.dto.basic.ParamMiddleware;
import uz.devops.humoMiddleware.dto.request.RetrieveCardV3RequestDTO;
import uz.devops.humoMiddleware.dto.response.RetrieveCardV3ResponseDTO;
import uz.devops.humoMiddleware.service.HumoMiddlewareService;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.paykap.gateway.CommonGatewaySender;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class HumoMiddlewareHelperService {

    public static final String ON = "on";
    private final HumoMiddlewareService humoMiddlewareService;
    private final CommonGatewaySender commonGatewaySender;

    public HasData<RetrieveCardV3ResponseDTO> retrieveCardV3(String pan) {
        log.debug("Request to retrieve card from humo with pan: {}", pan);
        return retrieveCardV3(getRetrieveCardV3Request(pan));
    }

    public HasData<RetrieveCardV3ResponseDTO> retrieveCardV3(RetrieveCardV3RequestDTO request) {

        log.debug("Request to retrieve card from humo : {}", request);

        ParamMiddleware<RetrieveCardV3RequestDTO> paramMiddleware = new ParamMiddleware<>();
        paramMiddleware.setParams(request);

        var middlewareSimpleResultData = humoMiddlewareService.retrieveCardV3(paramMiddleware);

        log.debug("Humo middleware retrieveCard result: {}", middlewareSimpleResultData.getData());

        return middlewareSimpleResultData;
    }

    public RetrieveCardV3RequestDTO getRetrieveCardV3Request(String pan) {
        return new RetrieveCardV3RequestDTO(pan, null, null, null, 1);
    }

//    public GetCardInfoRequestDTO getCardInfoRequestDTO(String realCard, String expiry1) {
//        GetCardInfoRequestDTO requestParams = new GetCardInfoRequestDTO();
//        requestParams.setCard(realCard);
//        if (!StringUtils.isEmpty(expiry1) && expiry1.length() > 6) {
//            requestParams.setExpiry(expiry1.substring(5, 7) + expiry1.substring(2, 4));
//        }
//
//        return requestParams;
//    }

//    public HasData<String> getPhoneNumber(String realCard, String expiry) {
//        return getPhoneNumber(getCardInfoRequestDTO(realCard, expiry));
//    }

//    public HasData<String> getPhoneNumber(GetCardInfoRequestDTO requestParams) {
//
//        log.info("send request to humo middleware: {}", requestParams);
//
//        final HasData<GetCardInfoResponseDTO> responseEnv = commonGatewaySender.sendAndReceive(requestParams, humoMiddlewareService::getCardInfo, result -> result.getRecords() == null || result.getRecords().size() == 0);
//
//        log.info("humo middleware return result: {}", responseEnv);
//
//        for (RecordInfo recordInfo : responseEnv.getData().getRecords()) {
//            if (recordInfo != null
//                    && !Objects.equals(recordInfo.getStatus(), "error")
//                    && recordInfo.getData() != null
//                    && !recordInfo.getData().isEmpty()) {
//                for (CardDataInfo cardDataInfo : recordInfo.getData()) {
//                    if (cardDataInfo.getAgreement() != null && Objects.equals(cardDataInfo.getAgreement().getState(), ON)) {
//                        log.info("Found phone {}", cardDataInfo.getAgreement().getPhone());
//                        return new BmResultData<>(BmResultStatus.SUCCESS, cardDataInfo.getAgreement().getPhone());
//                    }
//                }
//            }
//        }
//
//        return new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, "no phone found");
//    }

    public HasData<String> getPhoneNumber(String realPan) {

        final HasData<RetrieveCardV3ResponseDTO> resultData = commonGatewaySender.sendAndReceive(realPan, this::retrieveCardV3, result -> result.getCard() != null && result.getCard().getExpiry() != null);

        if (resultData == null) {
            return new CommonResultData<>(BmResultStatus.PC_ERROR, null, "humo return empty response");
        }

        if (!resultData.isSuccess()) {
            return new CommonResultData<>(BmResultStatus.PC_ERROR, null, resultData.getMessage());
        }
        if (resultData.getData() == null || resultData.getData().getMb() == null) {
            return new CommonResultData<>(BmResultStatus.PC_ERROR, null, resultData.getMessage());
        }
        return new CommonResultData<>(resultData.getData().getMb().getPhone());
    }

}
