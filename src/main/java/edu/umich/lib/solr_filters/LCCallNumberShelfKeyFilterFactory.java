package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilterFactory;

import java.util.Map;

/**
 * Factory for {@link LCCallNumberShelfKeyFilter}.
 *
 * @deprecated This factory is deprecated and will be removed in a future release.
 */
@Deprecated
public class LCCallNumberShelfKeyFilterFactory extends TokenFilterFactory {
    public static final String NAME = "lcCallNumberShelfKey";

    /** SPI no-arg constructor — do not use directly. */
    public LCCallNumberShelfKeyFilterFactory() {
        throw new UnsupportedOperationException("NAME");
    }

    public LCCallNumberShelfKeyFilterFactory(Map<String, String> aMap) {
        super(aMap);
    }

    @Override
    public LCCallNumberShelfKeyFilter create(TokenStream aTokenStream) {
        return new LCCallNumberShelfKeyFilter(aTokenStream);
    }

}
