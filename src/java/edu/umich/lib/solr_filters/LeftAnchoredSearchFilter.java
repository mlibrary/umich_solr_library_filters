package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.AttributeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LeftAnchoredSearchFilter extends TokenFilter {

  private static final Logger LOGGER = LoggerFactory
                                         .getLogger(ISBNNormalizerFilter.class);

  private final CharTermAttribute myTermAttribute =
    addAttribute(CharTermAttribute.class);
  private Integer current_position = 0;
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

  protected LeftAnchoredSearchFilter(TokenStream input) {
    super(input);
  }


  /**
   * Takes a set of tokens and returns them with their position appended
   * to the term (so [bill, dueber] becomes [bill1, dueber2]. When used
   * with a phrase query, will only allow matches that are left-anchored
   *
   * We deal with the positionIncrementAttribute so tokens that occupy
   * the same position ([[Bill,bill], [Dueber,dueber]) will have the
   * correct number appended.
   * @return boolean
   * @throws IOException
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      current_position = 0;
      return false;
    }

    String t = myTermAttribute.toString();
    current_position += posIncrAtt.getPositionIncrement();

    myTermAttribute.setEmpty().append(t + current_position.toString());
    return true;
  }
}
