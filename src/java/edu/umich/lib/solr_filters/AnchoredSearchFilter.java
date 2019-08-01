package edu.umich.lib.solr_filters;

import java.io.IOException;
import java.util.*;

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

    public static class SavedState {
        Integer tokenPosition = 0;
        AttributeSource.State state;
        String token;

        public SavedState(AttributeSource.State inputState, String tok, Integer pos) {
            state = inputState;
            tokenPosition = pos;
            token = tok;
        }
    }


    private List<SavedState> cache = null;
    private Iterator<String> iterator = null;
    private AttributeSource.State finalState = null;
    private Integer firstPosition = -1;
    private Integer lastPosition = -1;

    private State more_or_less_random_state = null;
    private Boolean alreadyOutputFirstToken = false;
    private Map<Integer, List<String>> tokensByPosition = new HashMap<>();

    /**
     * Create a new AnchoredSearchFilter around <code>input</code>. As with
     * any normal TokenFilter, do <em>not</em> call reset on the input; this filter
     * will do it normally.
     */
    AnchoredSearchFilter(TokenStream input) {
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
//            iterator = cache.iterator();
            iterator = joinedStrings(firstPosition, lastPosition, tokensByPosition).iterator();
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
            alreadyOutputFirstToken = false; // 'cause it persists
            return false;
        }



        restoreState(more_or_less_random_state);

        charTermAtt.setEmpty().append(iterator.next());
        LOGGER.info("About to return " + charTermAtt.toString());

        if (alreadyOutputFirstToken) {
            posIncrAtt.setPositionIncrement(0);
        } else {
            posIncrAtt.setPositionIncrement(1);
            alreadyOutputFirstToken = true;
        }

//        SavedState savedState = iterator.next();
//        restoreState(savedState.state);
//        mungeToken(savedState);
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

    public  List<String> joinedStrings(Integer i, Integer maxKey, Map<Integer, List<String>> tokenMap) {
        List<String> acc = new ArrayList<>();
        if (i > maxKey) {
            acc.add("");
            return acc;
        } else {
            for (String tok : tokenMap.get(i)) {
                for (String s : joinedStrings(i + 1, maxKey, tokenMap)) {
                    LOGGER.debug("Adding " + String.join("_", tok, s));
                    acc.add(String.join("_", tok, s));
                }
            }
        }
        return acc;
    }


    private void setUpCache() throws IOException {
        cache = new ArrayList<>(64);
        List<String> tokens = new ArrayList<>();
        tokensByPosition = new HashMap<>();
        Integer currentPosition = -1;
        while (input.incrementToken()) {
            String token = charTermAtt.toString();
            currentPosition += posIncrAtt.getPositionIncrement();

            LOGGER.debug("Token " + token + " at position " + currentPosition.toString());
            cache.add(new SavedState(captureState(), token, currentPosition));
        }

        more_or_less_random_state = cache.get(0).state;

        firstPosition = cache.get(0).tokenPosition;
        lastPosition = cache.get(cache.size() - 1).tokenPosition;

        for (SavedState s: cache) {
            if (!tokensByPosition.containsKey(s.tokenPosition)) {
                tokensByPosition.put(s.tokenPosition, new ArrayList<>());
            }
            tokensByPosition.get(s.tokenPosition).add(s.token);
        }
        LOGGER.debug("First/last are " + firstPosition.toString() + " / " + lastPosition.toString());


        // capture final state
        input.end();
        finalState = captureState();
//        iterator = cache.iterator();
        iterator = joinedStrings(firstPosition, lastPosition, tokensByPosition).iterator();
    }


    /**
     * If the underlying token stream was consumed and cached.
     */
    public boolean isCached() {
        return cache != null;
    }


}
