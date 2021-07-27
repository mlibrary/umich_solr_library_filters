package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FullyAnchoredSearchFilter extends TokenFilter {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ISBNNormalizerFilter.class);

  private final CharTermAttribute myTermAttribute =
      addAttribute(CharTermAttribute.class);
  private Integer current_position = 0;
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

  private List<State> cache = null;
  private Iterator<State> iterator = null;
  private AttributeSource.State finalState;

  protected FullyAnchoredSearchFilter(TokenStream input) {
    super(input);
  }

  private State startState = null;
  private Boolean taggedFirstToken = false;
  private Integer lastPosition = -1;

  private void fillCache() throws IOException {
    while (input.incrementToken()) {
      cache.add(captureState());
    }
    input.end();
    finalState = captureState();
  }

  @Override
  public void reset() throws IOException {
    if (cache == null) { // first time
      input.reset();
    } else {
      iterator = cache.iterator();
    }
  }

  @Override
  public final void end() {
    if (finalState != null) {
      restoreState(finalState);
    }
  }

  private Integer findLastPosition() {
    Iterator<State> iter = cache.iterator();
    Integer lastpos = 0;
    while (iter.hasNext()) {
      restoreState(iter.next());
      if (lastpos.equals(0)) {
        lastpos = posIncrAtt.getPositionIncrement();
      } else {
        lastpos += posIncrAtt.getPositionIncrement();
      }
    }
    return lastpos;
  }

  /**
   * Modify the input tokens such that a phrase match has to match
   * _exactly_. We do this by appending the position to the end of each
   * token, and appending "000" to the very last token. The phrase
   * searches must match from beginning to end.
   *
   * @return boolean
   * @throws IOException
   */
  @Override
  public final boolean incrementToken() throws IOException {

    if (cache == null) { // first-time
      cache = new ArrayList<>(64);
      fillCache();
      lastPosition = findLastPosition();
      iterator = cache.iterator();
    }

    if (!iterator.hasNext()) {
      cache = null;
      current_position = 0;
      lastPosition = -1;
      return false;
    }

    restoreState(iterator.next());

    String t = myTermAttribute.toString();
    current_position += posIncrAtt.getPositionIncrement();
    LOGGER.debug("Working on " + t + " with position " + current_position.toString());

    // If we're at the last position, append "000"; otherwise,
    // add the position
    // x y z => x1 y2 z000
    if (current_position.equals(lastPosition)) {
      addSuffixToTerm("000");
    } else {
      addSuffixToTerm(current_position.toString());
    }

    return true;
  }

  private void addSuffixToTerm(String suffix) {
    String t = myTermAttribute.toString();
    myTermAttribute.setEmpty().append(t).append(suffix);
  }
}
