package edu.umich.lib.solr_filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeSource;

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

    public class SavedState {
        public Integer tokenPosition = 0;
        public AttributeSource.State state;
        public String token;

        public SavedState(AttributeSource.State inputState, String tok, Integer pos) {
            state = inputState;
            tokenPosition = pos;
            token = tok;
        }
    }


    private List<SavedState> cache = null;
    private Iterator<SavedState> iterator = null;
    private AttributeSource.State finalState;
    private Integer firstPosition = -1;
    private Integer lastPosition;

    private CharTermAttribute charTermAttribute;


    /**
     * Create a new AnchoredSearchFilter around <code>input</code>. As with
     * any normal TokenFilter, do <em>not</em> call reset on the input; this filter
     * will do it normally.
     */
    public AnchoredSearchFilter(TokenStream input) {
        super(input);
    }

    /**
     * Propagates reset if incrementToken has not yet been called. Otherwise
     * it rewinds the iterator to the beginning of the cached list.
     */
    @Override
    public void reset() throws IOException {
        if (cache == null) {//first time
            input.reset();
        } else {
            iterator = cache.iterator();
        }
    }



    /**
     * The first time called, it'll read and cache all tokens from the input.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (cache == null) {//first-time
            setUpCache();
        }

        if (!iterator.hasNext()) {
            // the cache is exhausted, return false
            return false;
        }

        SavedState savedState = iterator.next();
        restoreState(savedState.state);
        mungeToken(savedState);
        return true;
    }

    public void mungeToken(SavedState s) {

        if (s.tokenPosition.equals(firstPosition)) {
            charTermAtt.setEmpty().append("AAA").append(s.token);
        }

        if (s.tokenPosition.equals(lastPosition)) {
            charTermAtt.append("ZZZ");
        } else {
            charTermAtt.append(s.tokenPosition.toString());
        }
    }

    @Override
    public final void end() {
        if (finalState != null) {
            restoreState(finalState);
        }
    }


    private void setUpCache() throws IOException {
        cache = new ArrayList<>(64);
        Integer currentPosition = 0;
        LOGGER.info("Got here for god's sake");
        while (input.incrementToken()) {
            String token = charTermAtt.toString();
            currentPosition += posIncrAtt.getPositionIncrement();

            LOGGER.info("Token " + token + " at position " + currentPosition.toString());
            cache.add(new SavedState(captureState(), token, currentPosition));
        }

        firstPosition = cache.get(0).tokenPosition;
        lastPosition = cache.get(cache.size() - 1).tokenPosition;

        LOGGER.info("First/last are " + firstPosition.toString() + " / " + lastPosition.toString());


        // capture final state
        input.end();
        finalState = captureState();
        iterator = cache.iterator();
    }


    /**
     * If the underlying token stream was consumed and cached.
     */
    public boolean isCached() {
        return cache != null;
    }


}
