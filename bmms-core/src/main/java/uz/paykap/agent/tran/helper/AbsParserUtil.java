package uz.paykap.agent.tran.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AbsParserUtil {

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    public static BigDecimal parseRate(String rate) throws NumberFormatException{
        return BigDecimal.valueOf(Double.parseDouble(rate)).multiply(BigDecimal.valueOf(100L));
    }

    public static LocalDate parseDate(String date) throws DateTimeParseException {
        if (date.contains(".")) {
            date = date.substring(0, date.indexOf("."));
        }
        return LocalDate.parse(date, dateTimeFormatter);
    }
}
