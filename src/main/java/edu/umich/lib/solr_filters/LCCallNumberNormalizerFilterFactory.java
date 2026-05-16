/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umich.lib.solr_filters;
import java.util.Map;
import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;

/**
 * Factory for {@link LCCallNoFilter}.
 *
 * @deprecated This factory is deprecated and will be removed in a future release.
 * @author dueberb
 */
@Deprecated
public class LCCallNumberNormalizerFilterFactory extends TokenFilterFactory {

    public static final String NAME = "lcCallNumberNormalizer";

    /** SPI no-arg constructor — do not use directly. */
    public LCCallNumberNormalizerFilterFactory() {
        throw new UnsupportedOperationException("NAME");
    }

    public LCCallNumberNormalizerFilterFactory(Map<String, String> args) {
		super(args);
	}

    @Override
    public LCCallNoFilter create(TokenStream input)
    {
        return new LCCallNoFilter(input);
    }
}
