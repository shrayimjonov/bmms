package uz.paykap.service.dto.p2p;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import uz.paykap.domain.enumeration.P2pProcessorType;

@Data
@ApiModel(description = "Объект запроса на P2P перевод")
public class P2pParamInfoRequest {

    @ApiModelProperty("ПЦ отправителя")
    @NotNull
    private P2pProcessorType senderPc;

    @ApiModelProperty("ПЦ получателя")
    @NotNull
    private P2pProcessorType recipientPc;

    @ApiModelProperty("Счет отправителя")
    @NotNull
    private String senderAccount;

    @ApiModelProperty("Счет получателя")
    @NotNull
    private String recipientAccount;

    @ApiModelProperty("Промо-код")
    private String promo;

    public P2pParamInfoRequest withSenderPc(P2pProcessorType senderPc) {
        this.senderPc = senderPc;
        return this;
    }

    public P2pParamInfoRequest withRecipientPc(P2pProcessorType recipientPc) {
        this.recipientPc = recipientPc;
        return this;
    }

    public P2pParamInfoRequest withSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
        return this;
    }

    public P2pParamInfoRequest withRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
        return this;
    }

    public P2pParamInfoRequest withPromo(String promo) {
        this.promo = promo;
        return this;
    }
}
