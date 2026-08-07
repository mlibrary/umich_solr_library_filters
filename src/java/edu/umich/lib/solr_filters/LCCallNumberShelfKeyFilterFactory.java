package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilterFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCCallNumberShelfKeyFilterFactory extends TokenFilterFactory {
    public LCCallNumberShelfKeyFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public LCCallNumberShelfKeyFilter create(TokenStream aTokenStream) {
        return new LCCallNumberShelfKeyFilter(aTokenStream);
    }

}
