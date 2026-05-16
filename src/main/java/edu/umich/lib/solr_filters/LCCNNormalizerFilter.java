package edu.umich.lib.solr_filters;

import edu.umich.lib.converters.LCCNNormalizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A Lucene {@link TokenFilter} that normalizes Library of Congress Control Number (LCCN)
 * tokens to canonical form as defined by the
 * <a href="http://www.loc.gov/marc/lccn-namespace.html#syntax">LC LCCN namespace syntax</a>.
 *
 * <p>Each token is passed to {@link LCCNNormalizer#normalize(String)}, which strips
 * whitespace, removes trailing slash qualifiers, lowercases the alphabetic prefix, and
 * zero-pads the serial number to six digits. Tokens that cannot be normalized are passed
 * through unchanged.
 */
public final class LCCNNormalizerFilter extends TokenFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LCCNNormalizerFilter.class);

    private final CharTermAttribute termAttribute =
            addAttribute(CharTermAttribute.class);

    /**
     * Creates a new {@code LCCNNormalizerFilter} wrapping the given token stream.
     *
     * @param input the upstream {@link TokenStream}
     */
    public LCCNNormalizerFilter(TokenStream input) {
        super(input);
    }

    /**
     * Advances to the next token and normalizes it to canonical LCCN form if possible.
     *
     * <p>If the token does not contain a recognizable LCCN, it is passed through
     * unchanged. Normalization failures are logged at DEBUG level.
     *
     * @return {@code true} if a token is available; {@code false} at end of stream
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        String t = termAttribute.toString();

        if (!t.isEmpty()) {
            try {
                termAttribute.setEmpty().append(LCCNNormalizer.normalize(t));
            } catch (IllegalArgumentException details) {
                LOGGER.debug("Could not normalize LCCN token '{}': {}", t, details.getMessage());
            }
        }

        return true;
    }
}
