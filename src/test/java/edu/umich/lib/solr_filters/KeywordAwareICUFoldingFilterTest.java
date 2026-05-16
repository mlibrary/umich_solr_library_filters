package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KeywordAwareICUFoldingFilter.
 *
 * <p>The filter applies UTR#30 Unicode folding to non-keyword tokens only; tokens marked
 * as keywords must pass through unchanged.
 */
public class KeywordAwareICUFoldingFilterTest {

    private List<String> fold(ManualTokenStream source) throws IOException {
        KeywordAwareICUFoldingFilter filter = new KeywordAwareICUFoldingFilter(source);
        CharTermAttribute termAtt = filter.addAttribute(CharTermAttribute.class);
        List<String> results = new ArrayList<>();
        filter.reset();
        while (filter.incrementToken()) {
            results.add(termAtt.toString());
        }
        filter.end();
        filter.close();
        return results;
    }

    // -----------------------------------------------------------------------
    // Keyword-aware behaviour
    // -----------------------------------------------------------------------

    @Test
    void keywordTokenNotFolded() throws IOException {
        // A keyword-marked token with accented characters must not be folded
        ManualTokenStream ts = new ManualTokenStream().add("café", true);
        assertEquals(List.of("café"), fold(ts));
    }

    @Test
    void nonKeywordTokenFolded() throws IOException {
        // The same token without the keyword flag must be folded
        ManualTokenStream ts = new ManualTokenStream().add("café", false);
        assertEquals(List.of("cafe"), fold(ts));
    }

    @Test
    void mixedKeywordAndNonKeyword() throws IOException {
        // In a multi-token stream, only non-keyword tokens are folded
        ManualTokenStream ts = new ManualTokenStream()
                .add("café", true)   // keyword → unchanged
                .add("café", false)  // non-keyword → folded
                .add("straße", true) // keyword → unchanged
                .add("straße", false);// non-keyword → folded
        assertEquals(List.of("café", "cafe", "straße", "strasse"), fold(ts));
    }

    // -----------------------------------------------------------------------
    // Folding behaviour (non-keyword tokens)
    // -----------------------------------------------------------------------

    @Test
    void foldsAccentedCharacters() throws IOException {
        ManualTokenStream ts = new ManualTokenStream().add("café", false);
        assertEquals(List.of("cafe"), fold(ts));
    }

    @Test
    void foldsGermanEszett() throws IOException {
        ManualTokenStream ts = new ManualTokenStream().add("straße", false);
        assertEquals(List.of("strasse"), fold(ts));
    }

    @Test
    void asciiPassesThrough() throws IOException {
        ManualTokenStream ts = new ManualTokenStream().add("hello", false);
        assertEquals(List.of("hello"), fold(ts));
    }

    @Test
    void handlesEmptyStream() throws IOException {
        ManualTokenStream ts = new ManualTokenStream();
        assertTrue(fold(ts).isEmpty());
    }
}
