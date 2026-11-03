package uz.paykap.service.mapper;

import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.paykap.service.dto.SvCardDTO;

import java.util.List;
import java.util.stream.Collectors;

public class SvCardMapper {
    public static SvCardDTO toSvCardV2DTO(SvCardV2DTO svCardDTO) {
        if (svCardDTO == null){
            return null;
        }

        SvCardDTO cardDTO = new SvCardDTO();
        cardDTO.setAacct(svCardDTO.getAacct());
        cardDTO.setBalance(svCardDTO.getBalance());
        cardDTO.setUsername(svCardDTO.getUsername());
        cardDTO.setExpiry(svCardDTO.getExpiry());
        cardDTO.setCardtype(svCardDTO.getCardtype());
        cardDTO.setCashbackAmount(svCardDTO.getCashbackAmount());
        cardDTO.setFullName(svCardDTO.getFullName());
        cardDTO.setHoldAmount(svCardDTO.getHoldAmount());
        cardDTO.setPan(svCardDTO.getPan());
        cardDTO.setPhone(svCardDTO.getPhone());
        cardDTO.setPincnt(svCardDTO.getPincnt());
        cardDTO.setSms(svCardDTO.getSms());
        cardDTO.setStatus(svCardDTO.getStatus());
        cardDTO.setToken(svCardDTO.getToken());

        return cardDTO;
    }

    public static List<SvCardDTO> toSvCardV2DTOs(List<SvCardV2DTO> svCardDTOs) {
        return svCardDTOs.stream().map(SvCardMapper::toSvCardV2DTO).collect(Collectors.toList());
    }
}
