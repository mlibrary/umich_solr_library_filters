package edu.umich.lib.converters;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LCCallNumberNormalizerTest {

    public String test_normalization(String s) {
        try {
            return LCCallNumberNormalizer.normalize((s));
        } catch(MalformedCallNumberException e) {
            return s + " is malformed";
        }
    }
    @Test
    public void error_case_1()  {
        String s = "HD5325.R12.1894. C22";
        assertEquals(s,"HD@@05325@R@12000@18940@C@22000", test_normalization(s));
    }

    @Test
    public void error_case_2() {
        String s = "BR75 .H4453.1674";
        assertEquals(s, "BR@@00075@H@44530@16740", test_normalization(s) );
    }
}
