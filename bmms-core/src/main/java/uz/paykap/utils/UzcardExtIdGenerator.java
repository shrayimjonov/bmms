package uz.paykap.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;

@Service
public class UzcardExtIdGenerator implements ExtIdGeneratorInterface {

    private static final int MAX_LENGTH = 15;

    @Override
    public String generate(String prefix, String extId) {

        if ((prefix + extId).length() > MAX_LENGTH)
            throw new CodedException(ProblemEnum.LIMIT_EXCEEDED);

        return String.join("", prefix, StringUtils.leftPad(extId, (MAX_LENGTH - prefix.length()), "0"));
    }
}
