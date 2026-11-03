package uz.paykap.service.munisWSDL.services.certificate;

import uz.devops.munisagetdto.CancelTransfer;
import uz.devops.munisagetdto.ReceiveTransfer;

public interface MunisAgentCertificateService {
    Boolean verify(ReceiveTransfer transferRequest);
    Boolean verify(CancelTransfer transferRequest);
}
