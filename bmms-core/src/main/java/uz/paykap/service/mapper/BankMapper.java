package uz.paykap.service.mapper;

import org.springframework.stereotype.Service;
import uz.javlon.svgate.domain.Bank;
import uz.paykap.service.dto.SimpleBankDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class BankMapper {

    public SimpleBankDTO toDto(Bank bank) {
        SimpleBankDTO simpleBankDTO = new SimpleBankDTO();
        simpleBankDTO.setCardBin(bank.getCardBin());
        simpleBankDTO.setCommission(bank.getCommission());
        simpleBankDTO.setLogo(bank.getLogo());
        simpleBankDTO.setName(bank.getName());
        return simpleBankDTO;
    }


    public List<SimpleBankDTO> toDto(List<Bank> banks) {
        if (banks == null) {
            return null;
        }

        List<SimpleBankDTO> list = new ArrayList<>(banks.size());
        for (Bank bank : banks) {
            list.add(toDto(bank));
        }

        return list;
    }
}
