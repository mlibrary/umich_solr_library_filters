package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilterFactory;
import org.apache.lucene.analysis.util.AbstractAnalysisFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeywordAwareICUFoldingFilterFactory extends ICUFoldingFilterFactory {
    public KeywordAwareICUFoldingFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public KeywordAwareICUFoldingFilter create(TokenStream aTokenStream) {
        return new KeywordAwareICUFoldingFilter(aTokenStream);
    }

    public AbstractAnalysisFactory getMultiTermComponent() {
        return this;
    }
}
