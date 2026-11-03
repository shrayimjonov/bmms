package uz.paykap.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.result.HasResult;
import uz.paykap.domain.ForeignPay;
import uz.paykap.service.dto.ForeignPayDTO;

import java.util.Optional;

/**
 * Service Interface for managing {@link uz.paykap.domain.ForeignPay}.
 */
public interface ForeignPayService {
    /**
     * Save a foreignPay.
     *
     * @param foreignPayDTO the entity to save.
     * @return the persisted entity.
     */
    ForeignPayDTO save(ForeignPayDTO foreignPayDTO);

    /**
     * Partially updates a foreignPay.
     *
     * @param foreignPayDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ForeignPayDTO> partialUpdate(ForeignPayDTO foreignPayDTO);

    /**
     * Get all the foreignPays.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ForeignPayDTO> findAll(Pageable pageable);

    /**
     * Get the "id" foreignPay.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ForeignPayDTO> findOne(Long id);

    /**
     * Delete the "id" foreignPay.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Optional<ForeignPay> getUserPayment(String login, String token, Long billId);

    HasResult sendVerification(String cardToken, Long billId, String phone, Long amountInCents);

    Result verify(String login, Long billId, String otp);
}
