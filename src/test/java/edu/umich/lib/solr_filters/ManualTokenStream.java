package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A minimal {@link TokenStream} whose tokens are populated manually, for use in unit tests.
 *
 * <p>Each token has a text value and an optional keyword flag. The keyword flag is
 * written to {@link KeywordAttribute} before each token is returned, allowing filters
 * that respect {@code KeywordAttribute} (such as {@link KeywordAwareICUFoldingFilter})
 * to be tested with controlled keyword/non-keyword input.
 *
 * <p>Usage:
 * <pre>{@code
 * ManualTokenStream ts = new ManualTokenStream()
 *     .add("café", true)   // keyword — must not be folded
 *     .add("café", false); // non-keyword — should be folded to "cafe"
 * }</pre>
 */
public class ManualTokenStream extends TokenStream {

    static class SimpleToken {
        final String text;
        final boolean keyword;

        SimpleToken(String text, boolean keyword) {
            this.text = text;
            this.keyword = keyword;
        }
    }

    private final List<SimpleToken> tokens = new ArrayList<>();
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);
    private Iterator<SimpleToken> iter = null;

    /** Adds a token with the given text and keyword flag. Returns {@code this} for chaining. */
    public ManualTokenStream add(String text, boolean keyword) {
        tokens.add(new SimpleToken(text, keyword));
        return this;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        iter = null;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (iter == null) {
            iter = tokens.iterator();
            clearAttributes();
        }
        if (!iter.hasNext()) {
            return false;
        }
        SimpleToken t = iter.next();
        termAttribute.setEmpty().append(t.text);
        keywordAttribute.setKeyword(t.keyword);
        return true;
    }
}
