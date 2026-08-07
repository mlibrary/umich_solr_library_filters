package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LeftAnchoredSearchFilter extends TokenFilter {

    private Integer current_position = 0;
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final CharTermAttribute myTermAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
    private final PositionLengthAttribute posLengthAttr = addAttribute(PositionLengthAttribute.class);

    protected LeftAnchoredSearchFilter(TokenStream input) {
        super(input);
    }



    private void reset_class_variables() {
        current_position = 0;
    }

    /**
     * Takes a set of tokens and returns them with their position appended
     * to the term (so [bill, dueber] becomes [bill1, dueber2]. When used
     * with a phrase query, will only allow matches that are left-anchored
     * <p>
     * We deal with the positionIncrementAttribute so tokens that occupy
     * the same position ([[Bill,bill], [Dueber,dueber]) will have the
     * correct number appended.
     *
     * @return boolean
     * @throws IOException
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            reset_class_variables();
            return false;
        }

        String t = myTermAttribute.toString();
        current_position += posIncrAtt.getPositionIncrement();

        String newtok = t + current_position.toString();
        myTermAttribute.setEmpty().append(newtok);
        return true;
    }
}
