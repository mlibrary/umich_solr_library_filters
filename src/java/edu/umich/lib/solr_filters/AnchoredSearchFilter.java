package edu.umich.lib.solr_filters;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
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


  private List<String> tokens = new ArrayList<String>();

  /**
   * Create a new AnchoredSearchFilter around <code>input</code>. As with
   * any normal TokenFilter, do <em>not</em> call reset on the input; this filter
   * will do it normally.
   */
  AnchoredSearchFilter(TokenStream input) {
    super(input);
  }

  /**
   * Clear out the list of tokens and reset.
   */
  @Override
  public void reset() throws IOException {
    tokens.clear();
    input.reset();
  }


  /**
   * The first time called, it'll read and cache all tokens from the input,
   * join them together with '_' and set that string as the first (and only)
   * token.
   */
  @Override
  public final boolean incrementToken() throws IOException {

    if (!input.incrementToken() || !tokens.isEmpty()) {
      tokens.clear();
      return false;
    }

    String t = charTermAtt.toString();
    if (!keywordAttr.isKeyword() && t != null && t.length() != 0) {
      tokens.add(t);
    }
    input.end();
    while (input.incrementToken()) {
      t = charTermAtt.toString();
      if (!keywordAttr.isKeyword() && t != null && t.length() != 0) {
        tokens.add(t);
      }
    }
    String joined = String.join("_", tokens);
    charTermAtt.resizeBuffer(joined.length());
    charTermAtt.setEmpty().append(joined);

    offsetAttr.setOffset(0, joined.length());

    posLengthAttr.setPositionLength(1);
    posIncrAtt.setPositionIncrement(1);

    return true;
  }
}
//    CharTermAttribute first_char_attribute = charTermAtt;
//    String initial_string = first_char_attribute.toString();
//    OffsetAttribute first_offset_attribute = offsetAttr;
//    PositionIncrementAttribute first_pos_attribute = posIncrAtt;
//    PositionLengthAttribute first_pos_length = posLengthAttr;
//
//    /* Did we get nothing? Return false */
//    if ((initial_string == null) || (initial_string.length() == 0)) {
//      return false;
//    }
//
//
//        /* On our first trip through, gather all the
//           tokens into a single array and set all the
//           tokens as empty.
//
//           We're also skipping the keywords. Smart? I don't know.
//         */
//
//    tokens.add(initial_string);
//
//    while (input.incrementToken()) {
//      String t = charTermAtt.toString();
//      charTermAtt.setEmpty();
//      if (!keywordAttr.isKeyword() && t != null && t.length() != 0) {
//        tokens.add(t);
//      }
//    }
//
//        /* Now we need to set the first charterm to hold the
//           joined set of all non-keyword tokens.
//         */
//    input.reset();
//    String joined = String.join("_", tokens);
//
//    LOGGER.info("Joined is " + joined);
//    LOGGER.info("First char attribute is " + first_char_attribute.toString());
//    LOGGER.info("Current char attribute is " + charTermAtt.toString());
//    charTermAtt.resizeBuffer(joined.length());
//    charTermAtt.setEmpty().append(joined);
//
//    offsetAttr.setOffset(0, joined.length());
//
//    posLengthAttr.setPositionLength(1);
//    posIncrAtt.setPositionIncrement(1);
//
//    LOGGER.info("Current char attribute is " + charTermAtt.toString());
//
//
//    while (input.incrementToken()) {
//      charTermAtt.setEmpty();
//    }
//
////    return true;
//  }
//}

