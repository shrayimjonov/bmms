package uz.paykap.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.repository.BankRepository;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class BankUtilService {

    public Bank validateAccountAndGetBank(String cardBinOrPan) {

        if (isNullOrEmpty(cardBinOrPan)) {
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "card bin is null or empty");
        }

        cardBinOrPan = StringUtils.substring(cardBinOrPan, 0, 6);

        log.debug("Card bin : {}", cardBinOrPan);

        Optional<Bank> bankOptional = bankRepository.findTop1ByCardBin(cardBinOrPan);

        if (!bankOptional.isPresent()) {
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "card bin " + cardBinOrPan + " not supported");
        }

        if (!bankOptional.get().isp2p()
            || isNullOrEmpty(bankOptional.get().getMerchantId())
            || isNullOrEmpty(bankOptional.get().getTerminalId())
            || bankOptional.get().getPort() == null) {
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "invalid epos data in DB, m = " + bankOptional.get().getMerchantId() + ", t=" + bankOptional.get().getTerminalId() + ", p=" + bankOptional.get().getPort());
        }

        return bankOptional.get();
    }

    public ResultData<Bank> getSupportedBank(String cardBinOrPan) {

        if (isNullOrEmpty(cardBinOrPan)) {
            return new ResultData<>(new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "card bin is null or empty"));
        }

        cardBinOrPan = StringUtils.substring(cardBinOrPan, 0, 6);

        log.debug("Card bin : {}", cardBinOrPan);

        Optional<Bank> bankOptional = bankRepository.findTop1ByCardBin(cardBinOrPan);

        if (!bankOptional.isPresent()) {
            return new ResultData<>(new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "card bin " + cardBinOrPan + " not supported"));
        }

        if (!bankOptional.get().isp2p()
            || isNullOrEmpty(bankOptional.get().getMerchantId())
            || isNullOrEmpty(bankOptional.get().getTerminalId())
            || bankOptional.get().getPort() == null) {
            return new ResultData<>(new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "invalid epos data in DB, m = " + bankOptional.get().getMerchantId() + ", t=" + bankOptional.get().getTerminalId() + ", p=" + bankOptional.get().getPort()));
        }

        return new ResultData<>(bankOptional.get());
    }

    private final BankRepository bankRepository;

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
