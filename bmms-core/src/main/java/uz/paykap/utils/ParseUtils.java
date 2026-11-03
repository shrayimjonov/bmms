package uz.paykap.utils;

import com.jayway.jsonpath.JsonPath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.xml.xpath.XPathUtils;
import uz.paykap.config.Constants;

@Slf4j
@UtilityClass
public class ParseUtils {

    public static String extractValueFromResponse(String response, String query, boolean isJsonResponse) {

        if (response == null || response.isEmpty()) {
            return Constants.INVALID_PARAMS_TEXT;
        }

        if (query == null || query.isEmpty()) {
            return Constants.NO_QUERY;
        }

        response = response.replaceAll("[\uFEFF-\uFFFF]", "");

        if (isJsonResponse) {
            return extractFromJson(response, query);
        } else {
            return extractFromXml(response, query);
        }

    }

    public static String extractFromXml(String response, String query) {
        try {
            String result = XPathUtils.evaluate(response, query);
            return result != null && !result.isEmpty() ? result : Constants.EMPTY_VALUE_TEXT;
        } catch (Exception e) {
            log.warn("XPath parse failed : content={}, xpath={}", response, query);
            return Constants.NOT_FOUND_TEXT;
        }
    }

    public static String extractFromJson(String response, String query) {
        try {
            String result = JsonPath.read(response, query) + "";
            return !result.isEmpty() ? result : Constants.EMPTY_VALUE_TEXT;
        } catch (Exception exception) {
            log.warn("JsonPath parse failed : content={}, xpath={}", response, query);
            return Constants.NOT_FOUND_TEXT;
        }
    }

}
