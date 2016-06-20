/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umich.lib.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;

/**
 *
 * @author dueberb
 */
public class LCCallNumberNormalizerFilterFactory extends TokenFilterFactory {
	
    public LCCallNumberNormalizerFilterFactory(Map<String, String> args) {
		super(args);
	}

    @Override
    public LCCallNoFilter create(TokenStream input)
    {
        return new LCCallNoFilter(input);
    }
}
