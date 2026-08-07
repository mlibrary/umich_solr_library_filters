package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilterFactory;

import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class LCCNNormalizerFilterFactory extends TokenFilterFactory {
    public LCCNNormalizerFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public LCCNNormalizerFilter create(TokenStream aTokenStream) {
        return new LCCNNormalizerFilter(aTokenStream);
    }

}
