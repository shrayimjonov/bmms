package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.paykap.service.bookkeeping.ParseToPostfix;
import uz.paykap.service.bookkeeping.RpnPostfixCalc;

@Service
public class CalculateUtils {

    private final static Logger log = LoggerFactory.getLogger(CalculateUtils.class);

    public Double calculateAmountRule(String s) {
        try {
            ParseToPostfix parser = new ParseToPostfix();
            RpnPostfixCalc rpnPostfixCalc = new RpnPostfixCalc();
            return rpnPostfixCalc.calculate(parser.listToString(parser.generatePostfixNotation(parser.parse(s))));
        } catch (Exception e) {
            log.warn("Parse error");
            return null;
        }
    }
}
