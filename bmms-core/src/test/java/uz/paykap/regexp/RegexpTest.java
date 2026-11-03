package uz.paykap.regexp;

import org.junit.Test;

public class RegexpTest {

    @Test
    public void testReplacePlus() {
        String expect = "998901234567";

        String actual;

        actual = removePrefix("+998901234567", "\\d{12}");
        assert actual.equals(expect);

        actual = removePrefix("998901234567", "\\d{12}");
        assert actual.equals(expect);
    }

    public String removePrefix(String text, String validation) {
//        return text.replaceAll("(\\+*)(\\d{12})", "$2");
        return text.replaceAll("^(.*)(" + validation + ")$", "$2");
    }

}
