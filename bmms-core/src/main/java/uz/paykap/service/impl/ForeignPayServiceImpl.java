package uz.paykap.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.otp.OtpGenerator;
import uz.javlon.commons.otp.SendStrategy;
import uz.javlon.commons.result.HasResult;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.commons.util.OtpUtil;
import uz.javlon.commons.util.PhoneUtil;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.ForeignPay;
import uz.paykap.domain.enumeration.ForeignPayStatus;
import uz.paykap.repository.ForeignPayRepository;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.dto.ForeignPayDTO;
import uz.paykap.service.mapper.ForeignPayMapper;
import uz.paykap.utils.NumberUtil;

import java.time.Instant;
import java.util.*;

/**
 * Service Implementation for managing {@link ForeignPay}.
 */
@Service
@Transactional
public class ForeignPayServiceImpl implements ForeignPayService {

    private final Logger log = LoggerFactory.getLogger(ForeignPayServiceImpl.class);

    private final ForeignPayRepository foreignPayRepository;

    private final ForeignPayMapper foreignPayMapper;
    private final OtpGenerator otpGenerator;
    private final ApplicationProperties properties;
    private final SendStrategy sendStrategy;

    public ForeignPayServiceImpl(
            ForeignPayRepository foreignPayRepository,
            ForeignPayMapper foreignPayMapper,
            OtpGenerator otpGenerator,
            ApplicationProperties properties,
            SendStrategy sendStrategy
    ) {
        this.foreignPayRepository = foreignPayRepository;
        this.foreignPayMapper = foreignPayMapper;
        this.otpGenerator = otpGenerator;
        this.properties = properties;
        this.sendStrategy = sendStrategy;
    }

    @Override
    public ForeignPayDTO save(ForeignPayDTO foreignPayDTO) {
        log.debug("Request to save ForeignPay : {}", foreignPayDTO);
        ForeignPay foreignPay = foreignPayMapper.toEntity(foreignPayDTO);
        foreignPay = foreignPayRepository.save(foreignPay);
        return foreignPayMapper.toDto(foreignPay);
    }

    @Override
    public Optional<ForeignPayDTO> partialUpdate(ForeignPayDTO foreignPayDTO) {
        log.debug("Request to partially update ForeignPay : {}", foreignPayDTO);

        return foreignPayRepository
                .findById(foreignPayDTO.getId())
                .map(
                        existingForeignPay -> {
                            foreignPayMapper.partialUpdate(existingForeignPay, foreignPayDTO);

                            return existingForeignPay;
                        }
                )
                .map(foreignPayRepository::save)
                .map(foreignPayMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForeignPayDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ForeignPays");
        return foreignPayRepository.findAll(pageable).map(foreignPayMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ForeignPayDTO> findOne(Long id) {
        log.debug("Request to get ForeignPay : {}", id);
        return foreignPayRepository.findById(id).map(foreignPayMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ForeignPay : {}", id);
        foreignPayRepository.deleteById(id);
    }

    public Optional<ForeignPay> getUserPayment(String login, String token, Long billId) {
        return foreignPayRepository.findTop1ByCreatedByAndCardTokenAndBillIdOrderByVerifiedDateDesc(login, token, billId);
    }

    public HasResult sendVerification(String maskedAccountInfo, Long billId, String phone, Long amountInCents) {

        ForeignPay foreignPay = new ForeignPay()
                .billId(billId)
                .phone(phone)
                .cardToken(maskedAccountInfo)
                .status(ForeignPayStatus.NEW)
                .otp(otpGenerator.generateActivationCode())
                .entryCount(0)
                .verifiedDate(Instant.now());

        foreignPayRepository.save(foreignPay);

        Map<String, String> params = new HashMap<>(5);
        params.put(Constants.OTP_PARAM, foreignPay.getOtp());
        params.put(Constants.AMOUNT_PARAM, NumberUtil.formatCents(amountInCents));
        params.put(Constants.PAN_PARAM, CardUtil.maskIfBankCard(maskedAccountInfo));

        HasResult sendStrategyResult = sendStrategy.send(phone, OtpUtil.resolvedMessage(properties.getOtp().getOtpTemplate(), params));

        if (!sendStrategyResult.isSuccess()) {
            foreignPay.setStatus(ForeignPayStatus.SMS_NOT_SENT);
            foreignPayRepository.save(foreignPay);
        }

        return sendStrategyResult;
    }

    public Result verify(String login, Long requestUniqueId, String otp) {
        log.debug("Login : {}, requestUniqueId : {}, otp : {}", login, requestUniqueId, otp);

        final Optional<ForeignPay> foreignPayOptional = foreignPayRepository.findTop1ByCreatedByAndBillIdOrderByVerifiedDateDesc(
                login,
                requestUniqueId
        );

        if (foreignPayOptional.isEmpty()) {
            return new Result(false, new CodedException(ProblemEnum.INVALID_REQUEST, "foreignPay not found"));
        }

        ForeignPay foreignPay = foreignPayOptional.get();

        log.debug("Check ForeignPayStatus : {}", foreignPay.getStatus());
        if (foreignPay.getStatus() != ForeignPayStatus.NEW) {
            return new Result(false, new CodedException(ProblemEnum.INVALID_REQUEST));
        }

        log.debug(
                "Check expiry, now : {}, expiry time : {}",
                Instant.now(),
                foreignPay.getVerifiedDate().plusSeconds(properties.getOtp().getTimeoutSeconds())
        );

        if (!foreignPay.getVerifiedDate().isAfter(Instant.now().minusSeconds(properties.getOtp().getTimeoutSeconds()))) {
            foreignPay.setStatus(ForeignPayStatus.EXPIRED);
            foreignPay = foreignPayRepository.save(foreignPay);
            sendVerification(foreignPay.getCardToken(), foreignPay.getBillId(), foreignPay.getPhone(), 0L); // TODO

            return new Result(
                    false,
                    new CodedException(
                            ProblemEnum.CODE_EXPIRED_RESTART,
                            Collections.singletonMap("phone", PhoneUtil.pretty(foreignPay.getPhone()))
                    )
            );
        }

        log.debug("Check otp, actual : {}, expect : {}", otp, foreignPay.getOtp());
        if (Objects.equals(foreignPay.getOtp(), otp)) {
            foreignPay.setStatus(ForeignPayStatus.VERIFIED);
            foreignPayRepository.save(foreignPay);
            return new Result(Boolean.TRUE, null);
        }

        Integer entryCount = foreignPay.getEntryCount() + 1;

        log.debug("Check limit, tries : {}, limit : {}", entryCount, properties.getOtp().getEntryLimit());
        if (entryCount > properties.getOtp().getEntryLimit()) {
            log.debug("To many retries, resend other code");
            foreignPay.setStatus(ForeignPayStatus.LIMIT_EXCEEDED);
            foreignPay = foreignPayRepository.save(foreignPay);
            sendVerification(foreignPay.getCardToken(), foreignPay.getBillId(), foreignPay.getPhone(), 0L);

            return new Result(
                    false,
                    new CodedException(
                            ProblemEnum.CODE_ENTRY_LIMIT_EXCEEDED_RESTART,
                            Collections.singletonMap("phone", PhoneUtil.pretty(foreignPay.getPhone()))
                    )
            );
        }

        log.debug("Before save ForeignPay : {}", foreignPay);
        foreignPay = foreignPayRepository.save(foreignPay.entryCount(entryCount));
        log.debug("After save ForeignPay : {}", foreignPay);

        Map<String, Object> map = new HashMap<>(2);
        map.put("left", String.valueOf(properties.getOtp().getEntryLimit() - entryCount));
        map.put("phone", PhoneUtil.pretty(foreignPay.getPhone()));

        return new Result(false, new CodedException(ProblemEnum.INVALID_CODE_RETRY, map));
    }
}
