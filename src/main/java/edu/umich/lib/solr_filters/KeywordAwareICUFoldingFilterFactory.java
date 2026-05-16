package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilterFactory;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeywordAwareICUFoldingFilterFactory extends ICUFoldingFilterFactory {
    public static final String NAME = "keywordAwareICUFolding";

    /** SPI no-arg constructor — do not use directly. */
    public KeywordAwareICUFoldingFilterFactory() {
        throw new UnsupportedOperationException("NAME");
    }

    public KeywordAwareICUFoldingFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public TokenStream create(TokenStream aTokenStream) {
        return new KeywordAwareICUFoldingFilter(aTokenStream);
    }


}
