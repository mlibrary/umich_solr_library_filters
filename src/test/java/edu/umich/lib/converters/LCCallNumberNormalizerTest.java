package edu.umich.lib.converters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCCallNumberNormalizer.
 *
 * The normalizer pads each component so that call numbers sort correctly:
 *   alpha  → right-padded to 3 chars with "@"
 *   num    → left-padded to 6 digits with "0"
 *   dec    → right-padded to 5 digits with "0"
 *   cutters → alpha kept + num right-padded to 5 + dec right-padded to 5
 * Components are joined with "@".
 */
public class LCCallNumberNormalizerTest {

    // -----------------------------------------------------------------------
    // match()
    // -----------------------------------------------------------------------

    @Test
    void matchReturnsTrueForSimpleCallNumber() {
        assertTrue(LCCallNumberNormalizer.match("QA76.9.A25 F74 2011"));
    }

    @Test
    void matchReturnsTrueForMinimalAlphaNum() {
        assertTrue(LCCallNumberNormalizer.match("QA 76"));
    }

    @Test
    void matchReturnsFalseForDigitOnlyInput() {
        assertFalse(LCCallNumberNormalizer.match("12345"));
    }

    @Test
    void matchReturnsFalseForPureWhitespace() {
        assertFalse(LCCallNumberNormalizer.match("   "));
    }

    // -----------------------------------------------------------------------
    // normalize() – basic cases
    // -----------------------------------------------------------------------

    @Test
    void normalizeSimpleAlphaNum() throws MalformedCallNumberException {
        // "QA 76" → alpha=QA(→"QA@"), num=76(→"000076"), no dec, no cutters
        String result = LCCallNumberNormalizer.normalize("QA 76");
        assertEquals("QA@@000076", result);
    }

    @Test
    void normalizeWithDecimal() throws MalformedCallNumberException {
        // "QA 76.9" → alpha=QA, num=76, dec=9
        String result = LCCallNumberNormalizer.normalize("QA 76.9");
        assertEquals("QA@@000076@90000", result);
    }

    @Test
    void normalizeWithOneCutter() throws MalformedCallNumberException {
        // "QA76 .A25" → alpha=QA, num=76, cutter1 = A 25
        String result = LCCallNumberNormalizer.normalize("QA76 .A25");
        assertEquals("QA@@000076@A@25000", result);
    }

    @Test
    void normalizeWithFullCutter() throws MalformedCallNumberException {
        // "QA76.9 .A25" → alpha=QA, num=76, dec=9, cutter1=A25
        String result = LCCallNumberNormalizer.normalize("QA76.9 .A25");
        assertEquals("QA@@000076@90000@A@25000", result);
    }

    @Test
    void normalizeSingleLetterAlpha() throws MalformedCallNumberException {
        // "B 12" → alpha right-padded to 3 = "B@@", num left-padded to 6 = "000012"
        // joined with "@" separator → "B@@" + "@" + "000012" = "B@@@000012"
        String result = LCCallNumberNormalizer.normalize("B 12");
        assertEquals("B@@@000012", result);
    }

    @Test
    void normalizeThreeLetterAlpha() throws MalformedCallNumberException {
        // "QAB 123" → alpha already 3 chars, no padding: "QAB"; num = "000123"
        // joined → "QAB" + "@" + "000123" = "QAB@000123"
        String result = LCCallNumberNormalizer.normalize("QAB 123");
        assertEquals("QAB@000123", result);
    }

    @Test
    void normalizeCaseInsensitive() throws MalformedCallNumberException {
        String upper = LCCallNumberNormalizer.normalize("QA 76");
        String lower = LCCallNumberNormalizer.normalize("qa 76");
        assertEquals(upper, lower);
    }

    // -----------------------------------------------------------------------
    // normalize() – exception cases
    // -----------------------------------------------------------------------

    @Test
    void normalizeThrowsWhenAlphaOnly() {
        assertThrows(MalformedCallNumberException.class, () ->
            LCCallNumberNormalizer.normalize("QA"));
    }

    @Test
    void normalizeThrowsForGarbage() {
        assertThrows(MalformedCallNumberException.class, () ->
            LCCallNumberNormalizer.normalize("not a call number"));
    }

    @Test
    void normalizeThrowsForFourLetterPrefix() {
        assertThrows(MalformedCallNumberException.class, () ->
            LCCallNumberNormalizer.normalize("ABCD 123"));
    }

    // -----------------------------------------------------------------------
    // Sorting order: normalized forms should sort correctly
    // -----------------------------------------------------------------------

    @Test
    void normalizedFormsSortCorrectly() throws MalformedCallNumberException {
        String qa10 = LCCallNumberNormalizer.normalize("QA 10");
        String qa100 = LCCallNumberNormalizer.normalize("QA 100");
        String qa9   = LCCallNumberNormalizer.normalize("QA 9");

        // QA 9 < QA 10 < QA 100 lexicographically after normalization
        assertTrue(qa9.compareTo(qa10) < 0,  "QA9 should sort before QA10");
        assertTrue(qa10.compareTo(qa100) < 0, "QA10 should sort before QA100");
    }
}
