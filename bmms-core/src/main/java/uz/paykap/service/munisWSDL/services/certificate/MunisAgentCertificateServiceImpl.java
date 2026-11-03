package uz.paykap.service.munisWSDL.services.certificate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.devops.munisagetdto.CancelTransfer;
import uz.devops.munisagetdto.ReceiveTransfer;
import uz.devops.munisagetdto.service.certificate.CBRUAfrMunAuthClient;
import uz.paykap.config.ApplicationProperties;

import javax.xml.ws.Holder;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Service("munisAgentCertificateService")
@ConditionalOnProperty(prefix = "application", name = "munisAgent.certificateServiceSimulate", havingValue = "false")
public class MunisAgentCertificateServiceImpl implements MunisAgentCertificateService {

    public static final String SUCCESS = "0";
    private final CBRUAfrMunAuthClient certificateClient;
    private final ApplicationProperties applicationProperties;

    @Override
    public Boolean verify(ReceiveTransfer transferRequest) {
        log.info("do certificate verification...");
        Holder<String> errorCode = new Holder<>();
        Holder<String> errorDesc = new Holder<>();
        Holder<String> signer = new Holder<>();
        try {
            certificateClient.getCBRUAfrMunAuthClientSoap()
                .verify(
                    transferRequest.getPayment().getSysinfo(),
                    transferRequest.getPayment().getPayer(),
                    transferRequest.getPayment().getPayee(),
                    transferRequest.getPayment().getAmount(),
                    transferRequest.getPayment().getDocument(),
                    transferRequest.getPayment().getDsign(),
                    errorCode,
                    errorDesc,
                    signer
                );
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return Objects.equals(errorCode.value, SUCCESS) && Objects.equals(signer.value, applicationProperties.getMunisAgent().getAnorBankMFo());
    }

    @Override
    public Boolean verify(CancelTransfer transferRequest) {
        Holder<String> errorCode = new Holder<>();
        Holder<String> errorDesc = new Holder<>();
        Holder<String> signer = new Holder<>();

        certificateClient.getCBRUAfrMunAuthClientSoap().verify(transferRequest.getSysinfo(), null, null, null, null, transferRequest.getDsign(), errorCode, errorDesc, signer);
        return Objects.equals(errorCode.value, SUCCESS) && Objects.equals(signer.value, applicationProperties.getMunisAgent().getAnorBankMFo());
    }
}
