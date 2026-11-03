package uz.paykap.service.p2p.dto;

import lombok.Data;

@Data
public class BaseP2pAccount<TParams> {
    private TParams params;
}
