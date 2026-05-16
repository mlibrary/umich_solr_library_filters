package edu.umich.lib.solr_filters;

import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * A keyword-aware variant of {@link ICUFoldingFilter} that applies UTR#30 Character
 * Foldings to non-keyword tokens only.
 *
 * <p>{@link ICUFoldingFilter} is declared {@code final} and cannot be subclassed.
 * This filter replicates its normalization logic while skipping tokens that have been
 * marked as keywords (e.g. by a {@code KeywordMarkerFilter} upstream).
 *
 * <p>The normalizer is sourced directly from {@link ICUFoldingFilter#NORMALIZER} so that
 * any future updates to the underlying UTR#30 data in the Lucene ICU jar are
 * automatically picked up.
 */
public final class KeywordAwareICUFoldingFilter extends TokenFilter {

    /**
     * The UTR#30 normalizer used for folding, delegated from {@link ICUFoldingFilter#NORMALIZER}.
     * Exposed as a constant to allow injection of a custom normalizer in tests via the
     * two-argument constructor.
     */
    public static final Normalizer2 NORMALIZER = ICUFoldingFilter.NORMALIZER;

    private final Normalizer2 normalizer;
    private final StringBuilder buffer = new StringBuilder();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);

    /**
     * Creates a new {@code KeywordAwareICUFoldingFilter} using the standard UTR#30 normalizer.
     *
     * @param input the upstream {@link TokenStream}
     */
    public KeywordAwareICUFoldingFilter(TokenStream input) {
        this(input, NORMALIZER);
    }

    /**
     * Creates a new {@code KeywordAwareICUFoldingFilter} with a custom normalizer.
     * Intended primarily for testing.
     *
     * @param input      the upstream {@link TokenStream}
     * @param normalizer the {@link Normalizer2} to apply
     */
    public KeywordAwareICUFoldingFilter(TokenStream input, Normalizer2 normalizer) {
        super(input);
        this.normalizer = normalizer;
    }

    /**
     * Advances to the next token. Keyword tokens are passed through unchanged.
     * Non-keyword tokens are folded using the UTR#30 normalizer if they are not
     * already in normalized form.
     *
     * @return {@code true} if a token is available; {@code false} at end of stream
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        if (keywordAttr.isKeyword()) {
            return true;
        }

        if (normalizer.quickCheck(termAtt) != Normalizer.YES) {
            buffer.setLength(0);
            posIncrAtt.setPositionIncrement(0);
            NORMALIZER.normalize(termAtt, buffer);
            termAtt.setEmpty().append(buffer);
        }
        return true;
    }
}
