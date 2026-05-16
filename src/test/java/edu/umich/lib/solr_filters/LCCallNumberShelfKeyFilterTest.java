package edu.umich.lib.solr_filters;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCCallNumberShelfKeyFilter.
 * Uses org.solrmarc.callnum.LCCallNumber#getShelfKey() internally.
 */
public class LCCallNumberShelfKeyFilterTest {

    private List<String> tokens(String input) throws Exception {
        return FilterTestHelper.getTokens(input, LCCallNumberShelfKeyFilter::new);
    }

    @Test
    void producesShelfKeyForSimpleCallNumber() throws Exception {
        List<String> result = tokens("QA76.9.A25");
        assertFalse(result.isEmpty(), "Filter should produce at least one token");
        // Shelf key is non-empty and starts with Q
        String key = result.get(0);
        assertFalse(key.isBlank());
        assertTrue(key.toUpperCase().startsWith("Q"),
                "Shelf key should start with Q for QA call numbers, got: " + key);
    }

    @Test
    void shelfKeysOrderCorrectly() throws Exception {
        String keyA = tokens("QA76").get(0);
        String keyB = tokens("QA100").get(0);
        String keyC = tokens("QA9").get(0);

        // QA9 < QA76 < QA100  (numeric ordering, not lexicographic)
        assertTrue(keyC.compareTo(keyA) < 0, "QA9 key should sort before QA76 key");
        assertTrue(keyA.compareTo(keyB) < 0, "QA76 key should sort before QA100 key");
    }

    @Test
    void handlesMultipleTokens() throws Exception {
        List<String> result = tokens("QA76 B12");
        assertEquals(2, result.size());
    }

    @Test
    void handlesEmptyInput() throws Exception {
        assertTrue(tokens("").isEmpty());
    }
}
