package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility for driving a single-input TokenFilter in tests without
 * pulling in lucene-test-framework.
 */
public final class FilterTestHelper {

    private FilterTestHelper() {}

    /**
     * Tokenise {@code input} with a WhitespaceTokenizer, wrap the stream
     * with the supplied filter factory, and return all resulting terms.
     */
    public static List<String> getTokens(String input,
                                          Function<TokenStream, TokenFilter> filterFactory)
            throws IOException {

        WhitespaceTokenizer wt = new WhitespaceTokenizer();
        wt.setReader(new StringReader(input));

        TokenFilter filter = filterFactory.apply(wt);
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
}
