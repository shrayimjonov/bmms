package uz.paykap.service.p2p.processor;

import lombok.Data;
import uz.paykap.service.p2p.dto.NciTranDTO;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class NciParam implements ProcessorParam {

    /*
     * Отправитель:
     * Транзитный счет банка. В NCI accCо. Пример : 23118...001
     * Если комиссия, доходный счет банка. В NCI accCо. Пример : 45294...001
     *
     * Получатель:
     * Транзитный счет банка. В NCI accCl. Пример : 17403...001
     * Если комиссия, доходный счет банка. В NCI accCl. Пример : 45294...001
     * */
    @NotNull
    private String bankAccount;

    //    @NotNull
    private String accName;

    //    @NotNull
    private String bankMfo;

    //    @NotNull
    private String purposeId;

    //    @NotNull
    private String purpose;

    private BigDecimal percent = new BigDecimal("100");

    private List<NciParam> commissions;

    @Deprecated
    private NciParam commission;

    private List<NciTranDTO> multiTranParams;
    private String tranType;
    private String tax;
}
