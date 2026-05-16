package edu.umich.lib.converters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ISBNNormalizer: extraction, conversion, and normalization.
 */
public class ISBNNormalizerTest {

    // -----------------------------------------------------------------------
    // isbn10_to_13
    // -----------------------------------------------------------------------

    @Test
    void isbn10_to_13_basicConversion() {
        // 0-306-40615-2  →  9780306406157
        assertEquals("9780306406157", ISBNNormalizer.isbn10_to_13("0306406152"));
    }

    @Test
    void isbn10_to_13_withXCheckDigit() {
        // ISBN-10 with X check digit: 0-19-853453-1 → just verifying round-trip logic
        // 019853453X → 9780198534532
        assertEquals("9780198534532", ISBNNormalizer.isbn10_to_13("019853453X"));
    }

    @Test
    void isbn10_to_13_leadingZero() {
        // 0-00-654323-1 → 9780006543237
        assertEquals("9780006543237", ISBNNormalizer.isbn10_to_13("0006543237"));
    }

    // -----------------------------------------------------------------------
    // extract_isbn13
    // -----------------------------------------------------------------------

    @Test
    void extractIsbn13_bareIsbn() {
        assertEquals("9780306406157", ISBNNormalizer.extract_isbn13("9780306406157"));
    }

    @Test
    void extractIsbn13_withHyphens() {
        assertEquals("9780306406157", ISBNNormalizer.extract_isbn13("978-0-306-40615-7"));
    }

    @Test
    void extractIsbn13_embeddedInString() {
        assertEquals("9780306406157", ISBNNormalizer.extract_isbn13("ISBN: 9780306406157 (hardcover)"));
    }

    @Test
    void extractIsbn13_978prefix() {
        assertEquals("9781234567897", ISBNNormalizer.extract_isbn13("9781234567897"));
    }

    @Test
    void extractIsbn13_979prefix() {
        assertEquals("9791032309681", ISBNNormalizer.extract_isbn13("9791032309681"));
    }

    @Test
    void extractIsbn13_throwsWhenAbsent() {
        assertThrows(IllegalArgumentException.class, () ->
            ISBNNormalizer.extract_isbn13("not an isbn at all"));
    }

    @Test
    void extractIsbn13_throwsOnIsbn10Only() {
        assertThrows(IllegalArgumentException.class, () ->
            ISBNNormalizer.extract_isbn13("0306406152"));
    }

    // -----------------------------------------------------------------------
    // extract_isbn10
    // -----------------------------------------------------------------------

    @Test
    void extractIsbn10_bareIsbn() {
        assertEquals("0306406152", ISBNNormalizer.extract_isbn10("0306406152"));
    }

    @Test
    void extractIsbn10_withHyphens() {
        assertEquals("0306406152", ISBNNormalizer.extract_isbn10("0-306-40615-2"));
    }

    @Test
    void extractIsbn10_embeddedInString() {
        assertEquals("0306406152", ISBNNormalizer.extract_isbn10("ISBN 0306406152."));
    }

    @Test
    void extractIsbn10_xCheckDigit() {
        assertEquals("019853453X", ISBNNormalizer.extract_isbn10("0-19-853453-X"));
    }

    @Test
    void extractIsbn10_throwsWhenAbsent() {
        assertThrows(IllegalArgumentException.class, () ->
            ISBNNormalizer.extract_isbn10("not an isbn"));
    }

    // -----------------------------------------------------------------------
    // normalize (top-level: prefers ISBN13, falls back to ISBN10→13)
    // -----------------------------------------------------------------------

    @Test
    void normalize_passesThrough13AsIs() {
        assertEquals("9780306406157", ISBNNormalizer.normalize("9780306406157"));
    }

    @Test
    void normalize_convertsIsbn10To13() {
        assertEquals("9780306406157", ISBNNormalizer.normalize("0306406152"));
    }

    @Test
    void normalize_handlesHyphenatedIsbn13() {
        assertEquals("9780306406157", ISBNNormalizer.normalize("978-0-306-40615-7"));
    }

    @Test
    void normalize_handlesHyphenatedIsbn10() {
        assertEquals("9780306406157", ISBNNormalizer.normalize("0-306-40615-2"));
    }

    @Test
    void normalize_throwsOnGarbage() {
        assertThrows(IllegalArgumentException.class, () ->
            ISBNNormalizer.normalize("not-an-isbn"));
    }

    @Test
    void normalize_prefersIsbn13WhenBothPresent() {
        // String contains both; 13 should win
        String result = ISBNNormalizer.normalize("9780306406157 (also 0306406152)");
        assertEquals("9780306406157", result);
    }
}
