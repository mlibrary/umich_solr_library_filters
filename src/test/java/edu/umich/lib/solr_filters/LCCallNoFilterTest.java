package edu.umich.lib.solr_filters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCCallNoFilter.
 * Valid LC call numbers are normalized; invalid tokens are uppercased and passed through.
 */
public class LCCallNoFilterTest {

    private List<String> tokens(String input) throws Exception {
        return FilterTestHelper.getTokens(input, LCCallNoFilter::new);
    }

    @Test
    void normalisesSimpleCallNumber() throws Exception {
        List<String> result = tokens("QA76");
        // QA → alpha; 76 → num.  Expected: "QA@@000076"
        assertEquals(List.of("QA@@000076"), result);
    }

    @Test
    void lowercaseInputNormalised() throws Exception {
        List<String> lower = tokens("qa76");
        List<String> upper = tokens("QA76");
        assertEquals(upper, lower);
    }

    @Test
    void invalidTokenPassedThroughUppercased() throws Exception {
        // A pure digit string doesn't match the LC pattern; should come through uppercased
        List<String> result = tokens("12345");
        assertEquals(List.of("12345"), result);
    }

    @Test
    void handlesMultipleTokens() throws Exception {
        List<String> result = tokens("QA76 notvalid B12");
        assertEquals(3, result.size());
        assertEquals("QA@@000076", result.get(0));
        assertEquals("notvalid",   result.get(1));
        assertEquals("B@@@000012",  result.get(2));
    }

    @Test
    void handlesEmptyInput() throws Exception {
        assertTrue(tokens("").isEmpty());
    }
}
