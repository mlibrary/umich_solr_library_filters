package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solrmarc.callnum.LCCallNumber;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCCallNumberShelfKeyFilter extends TokenFilter {
    /**
     * Logger used to log warnings.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LCCallNumberShelfKeyFilter.class);

    /**
     * The filter term that is a result of the conversion.
     */
    private final CharTermAttribute myTermAttribute =
            addAttribute(CharTermAttribute.class);

    /**
     * A Solr filter that parses ISO-639-1 and ISO-639-2 codes into English text
     * that can be used as a facet.
     *
     * @param aStream A {@link TokenStream} that parses streams with
     *                ISO-639-1 and ISO-639-2 codes
     */
    public LCCallNumberShelfKeyFilter(TokenStream aStream) {
        super(aStream);
    }

    /**
     * Increments and processes tokens in the ISO-639 code stream.
     *
     * @return True if a value is still available for processing in the token
     *         stream; otherwise, false
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        String t = myTermAttribute.toString();

        if (t != null && t.length() != 0) {
            try {
                LCCallNumber lcc = new LCCallNumber(t);
                myTermAttribute.setEmpty().append(lcc.getShelfKey());
            } catch (IllegalArgumentException details) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(details.getMessage(), details);
                }
            }
        }

        return true;
    }


}
