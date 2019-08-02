package edu.umich.lib.solr_filters;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.AttributeSource;

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sigh. Basically just a copy of CachingTokenFilter,  because once again
 * all the filters are declared final. So, I cut and paste all the code and then
 * add stuff to process for the anchored search.
 */
public class AnchoredSearchFilter extends TokenFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AnchoredSearchFilter.class);
    private final CharTermAttribute charTermAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
    private final PositionLengthAttribute posLengthAttr = addAttribute(PositionLengthAttribute.class);

    public static class TokenAndPosition {
        Integer tokenPosition = 0;
        String token;

        public TokenAndPosition(String tok, Integer pos) {
            tokenPosition = pos;
            token = tok;
        }
    }


    private List<TokenAndPosition> tokensAndPositions = null;
    private List<String> tokens = null;
    private Iterator<String> iterator = null;
    private AttributeSource.State finalState = null;
    private Integer firstPosition = -1;
    private Integer lastPosition = -1;

    private State more_or_less_random_state = null;
    private Boolean alreadyOutputFirstToken = false;
    private Map<Integer, List<String>> tokensByPosition = new HashMap<>();

    private void reset_everything() {
        tokensAndPositions = new ArrayList<>();
        tokens = new ArrayList<>();
        iterator = null;
        finalState = null;
        firstPosition = -1;
        lastPosition = -1;
        more_or_less_random_state = null;
        alreadyOutputFirstToken = false;
        tokensByPosition = new HashMap<>();
    }

    /**
     * Create a new AnchoredSearchFilter around <code>input</code>. As with
     * any normal TokenFilter, do <em>not</em> call reset on the input; this filter
     * will do it normally.
     */
    AnchoredSearchFilter(TokenStream input) {
        super(input);
        reset_everything();
    }

    /**
     * Propagates reset if incrementToken has not yet been called. Otherwise
     * it rewinds the iterator to the beginning of the cached list.
     */
    @Override
    public void reset() throws IOException {
        if (firstTime()) {//first time
            input.reset();
        } else {
//            iterator = cache.iterator();
            iterator = tokens.iterator();
        }
    }


    /**
     * The first time called, it'll read and cache all tokens from the input.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (firstTime()) {
            setUpCache();
        }

        if (!iterator.hasNext()) {
            // the cache is exhausted, return false
            reset_everything();
            return false;
        }

        restoreState(more_or_less_random_state);

        String tok = iterator.next();
        charTermAtt.resizeBuffer(tok.length());
        charTermAtt.setEmpty().append(tok);
        charTermAtt.setLength(tok.length());
        offsetAttr.setOffset(0, tok.length());
        posLengthAttr.setPositionLength(lastPosition - firstPosition + 1);


        LOGGER.debug("About to return " + charTermAtt.toString());

        if (alreadyOutputFirstToken) {
            posIncrAtt.setPositionIncrement(0);
        } else {
            posIncrAtt.setPositionIncrement(1);
            alreadyOutputFirstToken = true;
        }

        return true;
    }


    @Override
    public final void end() {
//        if (finalState != null) {
//            restoreState(finalState);
//        }
    }

    public List<String> joinedStrings(Integer i, Integer maxKey, Map<Integer, List<String>> tokenMap) {

        if (i.equals(maxKey)) {
            return tokenMap.get(i);
        }

        List<String> acc = new ArrayList<>();
        for (String tok : tokenMap.get(i)) {
            for (String s : joinedStrings(i + 1, maxKey, tokenMap)) {
                LOGGER.debug("Adding " + String.join("_", tok, s));
                acc.add(String.join("_", tok, s));
            }
        }

        return acc;
    }


    private void setUpCache() throws IOException {
        reset_everything();
        Integer currentPosition = -1;
        while (input.incrementToken()) {
            String token = charTermAtt.toString();
            currentPosition = currentPosition + posIncrAtt.getPositionIncrement();

            LOGGER.debug("Token " + token + " at position " + currentPosition.toString());
            tokensAndPositions.add(new TokenAndPosition(token, currentPosition));
        }

        // capture final state
        input.end();
        finalState = captureState();
        more_or_less_random_state = finalState;

        firstPosition = tokensAndPositions.get(0).tokenPosition;
        lastPosition = tokensAndPositions.get(tokensAndPositions.size() - 1).tokenPosition;

        for (TokenAndPosition s : tokensAndPositions) {
            if (!tokensByPosition.containsKey(s.tokenPosition)) {
                tokensByPosition.put(s.tokenPosition, new ArrayList<>());
            }
            tokensByPosition.get(s.tokenPosition).add(s.token);
        }
        LOGGER.debug("First/last are " + firstPosition.toString() + " / " + lastPosition.toString());

        tokens = joinedStrings(firstPosition, lastPosition, tokensByPosition);
        iterator = tokens.iterator();
    }

    private Boolean firstTime() {
        return tokensAndPositions.isEmpty();
    }

}
