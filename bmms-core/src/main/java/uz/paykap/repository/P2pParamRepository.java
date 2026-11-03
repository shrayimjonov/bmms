package uz.paykap.repository;

import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paykap.domain.P2pParam;
import uz.paykap.service.p2p.dto.P2pParamResult;

/**
 * Spring Data SQL repository for the P2pParam entity.
 */
@SuppressWarnings("unused")
@Repository
@JaversSpringDataAuditable
public interface P2pParamRepository extends JpaRepository<P2pParam, Long>, JpaSpecificationExecutor<P2pParam> {
    // TODO - get most match candidate
    //    Optional<P2pParam> findTop1ByActiveTrue();

    //    @Query("from P2pParam p where p.active = TRUE and p.senderPc = :senderPc and p.recipientPc = :recipientPc and (p.senderCondition.value = '%' OR p.senderCondition.value = :senderCondition OR p.recipientCondition.value = '*' OR p.recipientCondition.value = :recipientCondition) ORDER BY p.priority")
    //    Optional<P2pParam> findBestMatch(@Param("senderPc") P2pProcessorType senderPc, @Param("recipientPc") P2pProcessorType recipientPc, @Param("senderCondition") String senderCondition, @Param("recipientCondition") String recipientCondition);

    @Query(
        nativeQuery = true,
        value = "select p.priority, \n" +
        "       -- sender \n" +
        "       p.sender_pc as \"senderPc\", \n" +
        "       pcs.value as \"senderCondition\", \n" +
        "       coms.name as \"senderCommissionName\", \n" +
        "       coms.down as \"senderCommissionDown\", \n" +
        "       coms.up as \"senderCommissionUp\", \n" +
        "       coms.fee as \"senderCommissionFee\", \n" +
        "       -- recipient \n" +
        "       p.recipient_pc as \"recipientPc\", \n" +
        "       pcr.value as \"recipientCondition\", \n" +
        "       comr.name as \"recipientCommissionName\", \n" +
        "       comr.down as \"recipientCommissionDown\", \n" +
        "       comr.up as \"recipientCommissionUp\", \n" +
//        "       comr.fee as \"recipientCommissionFee\", \n" +
        "       -- common \n" +
        "       p.value, p.name \n" +
        "from p_2_p_param p \n" +
        "         join p_2_p_condition pcs on pcs.id = p.sender_condition_id \n" +
        "         join p_2_p_condition pcr on pcr.id = p.recipient_condition_id \n" +
        "         join p_2_p_commission coms on coms.id = p.sender_percent_id \n" +
        "         join p_2_p_commission comr on comr.id = p.recipient_percent_id \n" +
        "where p.active = TRUE \n" +
        "  and p.sender_pc = :senderPc \n" +
        "  and :senderCondition ~ pcs.value \n" +
        "  and p.recipient_pc = :recipientPc \n" +
        "  and :recipientCondition ~ pcr.value \n" +
        "ORDER BY p.priority DESC \n" +
        "limit 1"
    )
    Optional<P2pParamResult> findBestMatchNative2(
        @Param("senderPc") String senderPc,
        @Param("recipientPc") String recipientPc,
        @Param("senderCondition") String senderCondition,
        @Param("recipientCondition") String recipientCondition
    );

    @Query(
            nativeQuery = true,
            value = "select p.priority, \n" +
                    "       -- sender \n" +
                    "       p.sender_pc as \"senderPc\", \n" +
                    "       pcs.value as \"senderCondition\", \n" +
                    "       coms.name as \"senderCommissionName\", \n" +
                    "       coms.down as \"senderCommissionDown\", \n" +
                    "       coms.up as \"senderCommissionUp\", \n" +
                    "       coms.fee as \"senderCommissionFee\", \n" +
                    "       -- recipient \n" +
                    "       p.recipient_pc as \"recipientPc\", \n" +
                    "       pcr.value as \"recipientCondition\", \n" +
                    "       comr.name as \"recipientCommissionName\", \n" +
                    "       comr.down as \"recipientCommissionDown\", \n" +
                    "       comr.up as \"recipientCommissionUp\", \n" +
//        "       comr.fee as \"recipientCommissionFee\", \n" +
                    "       -- common \n" +
                    "       p.value, p.name \n" +
                    "from p_2_p_param p \n" +
                    "         join p_2_p_condition pcs on pcs.id = p.sender_condition_id \n" +
                    "         join p_2_p_condition pcr on pcr.id = p.recipient_condition_id \n" +
                    "         join p_2_p_commission coms on coms.id = p.sender_percent_id \n" +
                    "         join p_2_p_commission comr on comr.id = p.recipient_percent_id \n" +
                    "where p.active = TRUE \n" +
                    "  and p.sender_pc = :senderPc \n" +
                    "  and :senderCondition ~ pcs.value \n" +
                    "  and p.recipient_pc = :recipientPc \n" +
                    "  and :recipientCondition ~ pcr.value \n" +
                    "  and (p.promo is null OR cast(:promo AS text) ~ p.promo) \n" +
                    "ORDER BY p.priority DESC \n" +
                    "limit 1"
    )
    Optional<P2pParamResult> findBestMatchNative2WithPromo(
            @Param("senderPc") String senderPc,
            @Param("recipientPc") String recipientPc,
            @Param("senderCondition") String senderCondition,
            @Param("recipientCondition") String recipientCondition,
            @Param("promo") String promo
    );

    @Query(
        nativeQuery = true,
        value = "select p.priority, \n" +
        "       -- sender \n" +
        "       p.sender_pc as \"senderPc\", \n" +
        "       pcs.value as \"senderCondition\", \n" +
        "       coms.name as \"senderCommissionName\", \n" +
        "       coms.down as \"senderCommissionDown\", \n" +
        "       coms.up as \"senderCommissionUp\", \n" +
        "       coms.fee as \"senderCommissionFee\", \n" +
        "       -- recipient \n" +
        "       p.recipient_pc as \"recipientPc\", \n" +
        "       pcr.value as \"recipientCondition\", \n" +
        "       comr.name as \"recipientCommissionName\", \n" +
        "       comr.down as \"recipientCommissionDown\", \n" +
        "       comr.up as \"recipientCommissionUp\", \n" +
//        "       comr.fee as \"recipientCommissionFee\", \n" +
        "       -- common \n" +
        "       p.value, p.name \n" +
        "from p_2_p_param p \n" +
        "         join p_2_p_condition pcs on pcs.id = p.sender_condition_id \n" +
        "         join p_2_p_condition pcr on pcr.id = p.recipient_condition_id \n" +
        "         join p_2_p_commission coms on coms.id = p.sender_percent_id \n" +
        "         join p_2_p_commission comr on comr.id = p.recipient_percent_id \n" +
        "where p.active = TRUE \n" +
        "  and :senderCondition ~ pcs.value \n" +
        "  and :recipientCondition ~ pcr.value \n" +
        "ORDER BY p.priority DESC \n" +
        "limit 1"
    )
    Optional<P2pParamResult> findBestMatchNativeById(
        @Param("senderCondition") String senderCondition,
        @Param("recipientCondition") String recipientCondition
    );

    @Query(
        nativeQuery = true,
        value = "select p.priority, \n" +
        "       -- sender \n" +
        "       p.sender_pc as \"senderPc\", \n" +
        "       pcs.value as \"senderCondition\", \n" +
        "       coms.name as \"senderCommissionName\", \n" +
        "       coms.down as \"senderCommissionDown\", \n" +
        "       coms.up as \"senderCommissionUp\", \n" +
        "       coms.fee as \"senderCommissionFee\", \n" +
        "       -- recipient \n" +
        "       p.recipient_pc as \"recipientPc\", \n" +
        "       pcr.value as \"recipientCondition\", \n" +
        "       comr.name as \"recipientCommissionName\", \n" +
        "       comr.down as \"recipientCommissionDown\", \n" +
        "       comr.up as \"recipientCommissionUp\", \n" +
//        "       comr.fee as \"recipientCommissionFee\", \n" +
        "       -- common \n" +
        "       p.value, p.name \n" +
        "from p_2_p_param p \n" +
        "         join p_2_p_condition pcs on pcs.id = p.sender_condition_id \n" +
        "         join p_2_p_condition pcr on pcr.id = p.recipient_condition_id \n" +
        "         join p_2_p_commission coms on coms.id = p.sender_percent_id \n" +
        "         join p_2_p_commission comr on comr.id = p.recipient_percent_id \n" +
        "where p.active = TRUE \n" +
        "  and :senderCondition ~ pcs.value \n" +
        "  and :recipientCondition ~ pcr.value \n" +
        "  and (p.promo is null OR cast(:promo AS text) ~ p.promo) \n" +
        "ORDER BY p.priority DESC \n" +
        "limit 1"
    )
    Optional<P2pParamResult> findBestMatchNativeById(
        @Param("senderCondition") String senderCondition,
        @Param("recipientCondition") String recipientCondition,
        @Param("promo") String promo
    );

    List<P2pParam> findAllByActiveTrue();
}
