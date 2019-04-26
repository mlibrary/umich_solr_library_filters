package edu.umich.lib.solr_filters;

import com.ibm.icu.text.Normalizer;
import org.apache.lucene.analysis.TokenStream;
import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.ICUNormalizer2Filter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is straight-up copied from ICUNormalizer2Filter
 * because it and ICUFoldingFilter are
 * declared final so I can't inherit from either.
 */

public final class KeywordAwareICUFoldingFilter extends TokenFilter {
    /**
     * A normalizer for search term folding to Unicode text,
     * applying foldings from UTR#30 Character Foldings.
     */

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ISBNNormalizerFilter.class);

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final Normalizer2 normalizer;
    private final StringBuilder buffer = new StringBuilder();

    public static final Normalizer2 NORMALIZER = Normalizer2.getInstance(
            // TODO: if the wrong version of the ICU jar is used, loading these data files may give a strange error.
            // maybe add an explicit check? http://icu-project.org/apiref/icu4j/com/ibm/icu/util/VersionInfo.html
            ICUFoldingFilter.class.getResourceAsStream("utr30.nrm"),
            "utr30", Normalizer2.Mode.COMPOSE);

    /**
     * Need to figure out if it's a keyword or not...
     */

    private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);
    private final CharTermAttribute myTermAttribute =
            addAttribute(CharTermAttribute.class);

    /**
     * Create a new ICUFoldingFilter on the specified input
     */
    public KeywordAwareICUFoldingFilter(TokenStream input) {
        this(input, NORMALIZER);
    }

    public KeywordAwareICUFoldingFilter(TokenStream input, Normalizer2 normalizer) {
        super(input);
        this.normalizer = normalizer;
        LOGGER.info("We're actually here");

    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        String t = myTermAttribute.toString();
        if (keywordAttr.isKeyword()) {
            LOGGER.info(t + " is keyword");
            return true;
        }

        LOGGER.info(t + " is not a keyword");

        if (normalizer.quickCheck(termAtt) != Normalizer.YES) {
            buffer.setLength(0);
            NORMALIZER.normalize(termAtt, buffer);
            termAtt.setEmpty().append(buffer);
        }
        return true;

    }


}
