package uz.paykap.service.p2p.dto.response;

import lombok.Data;

@Data
public class P2pRateDto {
    private Long bankSell = 1L;
    private Long bankBuy = 1L;

    public P2pRateDto withBankSell(Long bankSell) {
        this.bankSell = bankSell;
        return this;
    }

    public P2pRateDto withBankBuy(Long bankBuy) {
        this.bankBuy = bankBuy;
        return this;
    }
}
