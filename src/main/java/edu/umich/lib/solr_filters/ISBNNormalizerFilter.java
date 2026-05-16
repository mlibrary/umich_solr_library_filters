package edu.umich.lib.solr_filters;

import edu.umich.lib.converters.ISBNNormalizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A Lucene {@link TokenFilter} that normalizes ISBN tokens to ISBN-13 format.
 *
 * <p>Each token is passed to {@link ISBNNormalizer#normalize(String)}, which extracts
 * and converts the ISBN to a canonical 13-digit form. Tokens that do not contain a
 * recognizable ISBN are passed through unchanged.
 */
public final class ISBNNormalizerFilter extends TokenFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ISBNNormalizerFilter.class);

    private final CharTermAttribute termAttribute =
            addAttribute(CharTermAttribute.class);

    /**
     * Creates a new {@code ISBNNormalizerFilter} wrapping the given token stream.
     *
     * @param input the upstream {@link TokenStream}
     */
    public ISBNNormalizerFilter(TokenStream input) {
        super(input);
    }

    /**
     * Advances to the next token and normalizes it to ISBN-13 if possible.
     *
     * <p>If the token does not contain a recognizable ISBN, it is passed through
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
                String normalized = ISBNNormalizer.normalize(t);
                termAttribute.setEmpty().append(normalized);
            } catch (IllegalArgumentException details) {
                LOGGER.debug("Could not normalize ISBN token '{}': {}", t, details.getMessage());
            }
        }

        return true;
    }
}
