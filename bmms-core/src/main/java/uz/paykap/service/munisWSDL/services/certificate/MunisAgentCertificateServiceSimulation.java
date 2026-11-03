package uz.paykap.service.munisWSDL.services.certificate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.devops.munisagetdto.CancelTransfer;
import uz.devops.munisagetdto.ReceiveTransfer;

@Slf4j
@Service("munisAgentCertificateService")
@ConditionalOnProperty(prefix = "application", name = "munisAgent.certificateServiceSimulate", havingValue = "true", matchIfMissing = true)
public class MunisAgentCertificateServiceSimulation implements MunisAgentCertificateService {

    public MunisAgentCertificateServiceSimulation() {
        log.info("##### Munis Simulation ON #####");
    }

    @Override
    public Boolean verify(ReceiveTransfer transferRequest) {
        return true;
    }

    @Override
    public Boolean verify(CancelTransfer transferRequest) {
        return true;
    }
}
