package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CompanyUtil {

    private static final Pattern companyNameQuoted = Pattern.compile("\"([^\"]*)\"");

    public static String getShortName(String companyName) {

        Matcher m = companyNameQuoted.matcher(companyName);

        if (m.find()) {
            return m.group(1);
        }

        return companyName;
    }

}
