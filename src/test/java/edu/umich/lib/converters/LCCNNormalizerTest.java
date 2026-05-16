package edu.umich.lib.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCCNNormalizer following the normalization rules at
 * http://www.loc.gov/marc/lccn-namespace.html#syntax
 *
 * <p>The bulk of test cases are ported from the Business::LCCN Perl module test suite
 * (library_stdnums gem spec). URL-prefixed inputs (http://lccn.loc.gov/…) are stripped to
 * the bare LCCN before being passed to the normalizer, because the Java implementation
 * does not handle URLs.
 */
public class LCCNNormalizerTest {

    // -----------------------------------------------------------------------
    // Parameterized: Business::LCCN Perl module cases
    // -----------------------------------------------------------------------

    static Stream<Arguments> lccnCases() {
        return Stream.of(
            // orig → normalized (from library_stdnums Ruby spec, LCCN section)
            Arguments.of("n78-890351",       "n78890351"),
            Arguments.of("n 78890351 ",      "n78890351"),
            Arguments.of(" 85000002 ",       "85000002"),
            Arguments.of("85-2 ",            "85000002"),
            Arguments.of("2001-000002",      "2001000002"),
            Arguments.of("75-425165//r75",   "75425165"),
            Arguments.of(" 79139101 /AC/r932", "79139101"),
            Arguments.of("89-4",             "89000004"),
            Arguments.of("89-45",            "89000045"),
            Arguments.of("89-456",           "89000456"),
            Arguments.of("89-1234",          "89001234"),
            Arguments.of("89-001234",        "89001234"),
            Arguments.of("89001234",         "89001234"),
            Arguments.of("2002-1234",        "2002001234"),
            Arguments.of("2002-001234",      "2002001234"),
            Arguments.of("2002001234",       "2002001234"),
            Arguments.of("   89001234 ",     "89001234"),
            Arguments.of("  2002001234",     "2002001234"),
            Arguments.of("a89-1234",         "a89001234"),
            Arguments.of("a89-001234",       "a89001234"),
            Arguments.of("a89001234",        "a89001234"),
            Arguments.of("a2002-1234",       "a2002001234"),
            Arguments.of("a2002-001234",     "a2002001234"),
            Arguments.of("a2002001234",      "a2002001234"),
            Arguments.of("a 89001234 ",      "a89001234"),
            Arguments.of("a 89-001234 ",     "a89001234"),
            Arguments.of("a 2002001234",     "a2002001234"),
            Arguments.of("ab89-1234",        "ab89001234"),
            Arguments.of("ab89-001234",      "ab89001234"),
            Arguments.of("ab89001234",       "ab89001234"),
            Arguments.of("ab2002-1234",      "ab2002001234"),
            Arguments.of("ab2002-001234",    "ab2002001234"),
            Arguments.of("ab2002001234",     "ab2002001234"),
            Arguments.of("ab 89001234 ",     "ab89001234"),
            Arguments.of("ab 2002001234",    "ab2002001234"),
            Arguments.of("ab 89-1234",       "ab89001234"),
            Arguments.of("abc89-1234",       "abc89001234"),
            Arguments.of("abc89-001234",     "abc89001234"),
            Arguments.of("abc89001234",      "abc89001234"),
            Arguments.of("abc89001234 ",     "abc89001234"),
            // URL-prefixed: strip http://lccn.loc.gov/ before normalizing
            Arguments.of("89001234",         "89001234"),   // from http://lccn.loc.gov/89001234
            Arguments.of("a89001234",        "a89001234"),  // from http://lccn.loc.gov/a89001234
            Arguments.of("ab89001234",       "ab89001234"), // from http://lccn.loc.gov/ab89001234
            Arguments.of("abc89001234",      "abc89001234"),// from http://lccn.loc.gov/abc89001234
            Arguments.of("2002001234",       "2002001234"), // from http://lccn.loc.gov/2002001234
            Arguments.of("a2002001234",      "a2002001234"),// from http://lccn.loc.gov/a2002001234
            Arguments.of("ab2002001234",     "ab2002001234"),// from http://lccn.loc.gov/ab2002001234
            Arguments.of("00-21595",         "00021595"),
            Arguments.of("2001001599",       "2001001599"),
            Arguments.of("99-18233",         "99018233"),
            Arguments.of("98000595",         "98000595"),
            Arguments.of("99005074",         "99005074"),
            Arguments.of("00003373",         "00003373"),
            Arguments.of("01001599",         "01001599"),
            Arguments.of("   95156543 ",     "95156543"),
            Arguments.of("   94014580 /AC/r95", "94014580"),
            Arguments.of("   79310919 //r86",   "79310919"),
            Arguments.of("gm 71005810  ",    "gm71005810"),
            Arguments.of("sn2006058112  ",   "sn2006058112"),
            Arguments.of("gm 71-2450",       "gm71002450"),
            Arguments.of("2001-1114",        "2001001114")
        );
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("lccnCases")
    void normalizesCorrectly(String input, String expected) {
        assertEquals(expected, LCCNNormalizer.normalize(input));
    }

    // -----------------------------------------------------------------------
    // Additional edge cases not covered by the Ruby spec
    // -----------------------------------------------------------------------

    @Test
    void lowercasesPrefix() {
        assertEquals("n79018997", LCCNNormalizer.normalize("N79018997"));
    }

    @Test
    void singleDigitPostHyphenPaddedToSix() {
        assertEquals("n79000001", LCCNNormalizer.normalize("n79-1"));
    }

    @Test
    void noHyphenReturnsPrefixOnly() {
        // "n790" has no hyphen and no recognisable structure — returned stripped/lowercased
        assertEquals("n790", LCCNNormalizer.normalize("n790"));
    }

    @Test
    void twoLetterPrefix() {
        assertEquals("sf79000025", LCCNNormalizer.normalize("sf 79-25"));
    }

    @Test
    void spacesAroundHyphen() {
        assertEquals("n79018997", LCCNNormalizer.normalize("n 79 - 18997"));
    }

    @Test
    void blankInputReturnsEmpty() {
        assertEquals("", LCCNNormalizer.normalize("   "));
    }
}
