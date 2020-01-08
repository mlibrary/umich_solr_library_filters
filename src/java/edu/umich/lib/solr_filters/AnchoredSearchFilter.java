package edu.umich.lib.solr_filters;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.AttributeSource;

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
  private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);


  public class TokenWithKeywordStatus {
    String token;
    Boolean isKeyword;

    public TokenWithKeywordStatus(String tok, Boolean keyword) {
      token = tok;
      isKeyword = keyword;
    }
  }

  public class TokenAndPosition {
    Integer tokenPosition = 0;
    String token;
    Boolean isKeyword;

    public TokenAndPosition(String tok, Integer pos, Boolean keyword) {
      tokenPosition = pos;
      token = tok;
      isKeyword = keyword;
    }
  }


  private List<TokenWithKeywordStatus> tokensWithKeywordStatus = null;
  private List<TokenAndPosition> tokensAndPositions = null;
  private List<String> tokens = null;
  private Iterator<TokenWithKeywordStatus> iterator = null;
  private AttributeSource.State finalState = null;
  private Integer lastPosition = -1;

  private State more_or_less_random_state = null;
  private Boolean alreadyOutputFirstToken = false;
  private Map<Integer, List<String>> nonKeyWordTokensByPosition = new HashMap<>();
  private Map<Integer, List<String>> keywordTokensByPosition = new HashMap<>();

  private final Integer defaultIndexOfFirstToken = 1;

  private void reset_everything() {
    tokensWithKeywordStatus = new ArrayList<>();
    tokensAndPositions = new ArrayList<>();
    tokens = new ArrayList<>();
    iterator = null;
    finalState = null;
    lastPosition = -1;
    more_or_less_random_state = null;
    alreadyOutputFirstToken = false;
    nonKeyWordTokensByPosition = new HashMap<>();
    keywordTokensByPosition = new HashMap<>();
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

  AnchoredSearchFilter() {
    super(new StandardTokenizer());
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
      iterator = tokensWithKeywordStatus.iterator();
    }
  }


  /**
   * The first time called, it'll read and cache all tokens from the input.
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if (firstTime()) {
      reset_everything();
      setUpCache();
    }

    if (!iterator.hasNext()) {
      // the cache is exhausted, return false
      reset_everything();
      return false;
    }

    restoreState(more_or_less_random_state);

    TokenWithKeywordStatus tk = iterator.next();
    String tok = tk.token;
    charTermAtt.resizeBuffer(tok.length());
    charTermAtt.setEmpty().append(tok);
    charTermAtt.setLength(tok.length());
    offsetAttr.setOffset(0, tok.length());

    // Look out for weridness with tokens get eliminated and thus
    // have the same position

    int poslength = lastPosition - defaultIndexOfFirstToken + 1;
    if (poslength < 0) {
      poslength = 0;
    }

    posLengthAttr.setPositionLength(poslength);
    keywordAttr.setKeyword(tk.isKeyword);

    LOGGER.debug("About to return " + tok);

    if (!alreadyOutputFirstToken) {
      posIncrAtt.setPositionIncrement(1);
      alreadyOutputFirstToken = true;
    } else {
      posIncrAtt.setPositionIncrement(0);
    }

    return true;
  }


  @Override
  public final void end() {
    if (finalState != null) {
      restoreState(finalState);
    }
  }

  public List<String> joinedTokens(Map<Integer, List<String>> map) {
    List<Integer> keys = new ArrayList<>(map.keySet());
    if (keys.isEmpty()) {
      return new ArrayList<>();
    }

    Collections.sort(keys);
    Integer maxPosition = keys.get(keys.size() - 1);
    lastPosition = maxPosition;
    return joinedTokens(defaultIndexOfFirstToken, maxPosition, map);
  }

  public List<String> joinedTokens(Integer currentIndex, Integer maxKey, Map<Integer, List<String>> map) {
    List<String> acc = new ArrayList<>();

    LOGGER.debug("Getting from " + currentIndex.toString() + " based on max of " + maxKey.toString());
    if (map.isEmpty()) {
      return acc;
    }
    if (currentIndex.equals(maxKey)) {
      return map.get(currentIndex);
    }

    for (String tok : map.get(currentIndex)) {
      for (String s : joinedTokens(currentIndex + 1, maxKey, map)) {
        acc.add(String.join("_", tok, s));
      }
    }
    return acc;
  }


  private void addTokenInfo(Map<Integer, List<String>> map, String token, Integer position) {
    if (!map.containsKey(position)) {
      map.put(position, new ArrayList<>());
    }
    map.get(position).add(token);
  }

  private void setUpCache() throws IOException {
    reset_everything();
    Integer currentPosition = defaultIndexOfFirstToken - 1;
    while (input.incrementToken()) {
      if (currentPosition == defaultIndexOfFirstToken - 1) {
        more_or_less_random_state = captureState();
      }
      String token = charTermAtt.toString();
      currentPosition = currentPosition + posIncrAtt.getPositionIncrement();

      LOGGER.debug("Token " + token + " at position " + currentPosition.toString());

      if (keywordAttr.isKeyword()) {
        addTokenInfo(keywordTokensByPosition, token, currentPosition);
      } else {
        addTokenInfo(nonKeyWordTokensByPosition, token, currentPosition);
      }
    }

    // capture final state
    input.end();
    finalState = captureState();


    for (String tok : joinedTokens(keywordTokensByPosition)) {
      LOGGER.debug("Adding keyword token " + tok);
      tokensWithKeywordStatus.add(new TokenWithKeywordStatus(tok, true));
    }

    for (String tok : joinedTokens(nonKeyWordTokensByPosition)) {
      LOGGER.debug("Adding nonKeyword token " + tok);
      tokensWithKeywordStatus.add(new TokenWithKeywordStatus(tok, false));
    }

    LOGGER.debug("TWKS list has size " + tokensWithKeywordStatus.size());
    iterator = tokensWithKeywordStatus.iterator();


  }

  private Boolean firstTime() {
    return tokensWithKeywordStatus.isEmpty();
  }

}
