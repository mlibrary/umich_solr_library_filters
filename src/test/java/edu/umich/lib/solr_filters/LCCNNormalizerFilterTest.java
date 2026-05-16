package edu.umich.lib.solr_filters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCCNNormalizerFilter.
 */
public class LCCNNormalizerFilterTest {

    private List<String> tokens(String input) throws Exception {
        return FilterTestHelper.getTokens(input, LCCNNormalizerFilter::new);
    }

    @Test
    void normalisesBasicLCCN() throws Exception {
        List<String> result = tokens("n79018997");
        assertEquals(List.of("n79018997"), result);
    }

    @Test
    void padsShortPostHyphenDigits() throws Exception {
        List<String> result = tokens("n79-18997");
        assertEquals(List.of("n79018997"), result);
    }

    @Test
    void lowercasesAndStripsSpaces() throws Exception {
        List<String> result = tokens("N79018997");
        assertEquals(List.of("n79018997"), result);
    }

    @Test
    void handlesMultipleTokens() throws Exception {
        List<String> result = tokens("n79-18997 sf79-25");
        assertEquals(List.of("n79018997", "sf79000025"), result);
    }

    @Test
    void handlesEmptyInput() throws Exception {
        assertTrue(tokens("").isEmpty());
    }
}
