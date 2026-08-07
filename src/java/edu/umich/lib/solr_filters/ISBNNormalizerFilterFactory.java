package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilterFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ISBNNormalizerFilterFactory extends TokenFilterFactory {
    public ISBNNormalizerFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public ISBNNormalizerFilter create(TokenStream aTokenStream) {
        return new ISBNNormalizerFilter(aTokenStream);
    }

}
