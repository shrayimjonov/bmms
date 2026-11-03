package uz.paykap.service.dto.p2p;

import org.apache.commons.lang3.StringUtils;
import uz.paykap.service.p2p.dto.*;

import java.util.Objects;
import java.util.Optional;

public enum P2pAccountType {

    PAN(P2pPanDTO.class),
    TOKEN(P2pTokenDTO.class),
    ACCOUNT(P2pAccountDTO.class),
    INN(P2pInnDTO.class),
    FOREX(P2pForexDTO.class),
    ANOR(P2pAnorDTO.class),
    DEPOSIT(P2pDepositDTO.class),
    //
    ;

    private final Class<?> dtoClass;

    P2pAccountType(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    public Class<?> getDtoClass() {
        return dtoClass;
    }

    public static P2pAccountType getByName(String name) {

        for (P2pAccountType type : P2pAccountType.values()) {
            if (StringUtils.equalsIgnoreCase(type.name(), name)) {
                return type;
            }
        }

        return PAN;
    }

    public static Optional<P2pAccountType> getByDtoClass(Class<?> dtoClass) {
        for (P2pAccountType type : P2pAccountType.values()) {
            if (Objects.equals(type.getDtoClass(), dtoClass)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }
}
