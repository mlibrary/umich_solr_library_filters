package edu.umich.lib.solr_filters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ISBNNormalizerFilter.
 * The filter normalises valid ISBNs to ISBN-13; invalid tokens are left as-is.
 */
public class ISBNNormalizerFilterTest {

    private List<String> tokens(String input) throws Exception {
        return FilterTestHelper.getTokens(input, ISBNNormalizerFilter::new);
    }

    @Test
    void normalisesIsbn13() throws Exception {
        List<String> result = tokens("9780306406157");
        assertEquals(List.of("9780306406157"), result);
    }

    @Test
    void convertsIsbn10ToIsbn13() throws Exception {
        List<String> result = tokens("0306406152");
        assertEquals(List.of("9780306406157"), result);
    }

    @Test
    void leavesInvalidTokenUnchanged() throws Exception {
        // An invalid ISBN string should be left as the original token
        // (the filter catches IllegalArgumentException and keeps the original)
        List<String> result = tokens("notanisbn");
        assertEquals(List.of("notanisbn"), result);
    }

    @Test
    void handlesMultipleTokens() throws Exception {
        List<String> result = tokens("0306406152 notanisbn 9780306406157");
        assertEquals(List.of("9780306406157", "notanisbn", "9780306406157"), result);
    }

    @Test
    void handlesEmptyInput() throws Exception {
        List<String> result = tokens("");
        assertTrue(result.isEmpty());
    }
}
