package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManualTokenStream extends TokenStream {

  static class SimpleToken {
    public Integer position;
    public String text;

    public SimpleToken(String txt, Integer pos) {
      position = pos;
      text = txt;
    }
  }

  List<SimpleToken> tokens = new ArrayList<>();
  private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private Integer currentTokenPosition = 0;
  private Iterator<SimpleToken> iter = null;


  public ManualTokenStream add(String txt, Integer pos) {
    tokens.add(new SimpleToken(txt, pos));
    return this;
  }


  @Override
  final public boolean incrementToken() throws IOException {
    if (iter == null) {
      iter = tokens.iterator();
      clearAttributes();
    }

    if (!iter.hasNext()) {
      return false;
    }

    SimpleToken t = iter.next();
    termAttribute.setEmpty().append(t.text);
    if (currentTokenPosition.equals(t.position)) {
      posIncrAtt.setPositionIncrement(0);
    } else {
      posIncrAtt.setPositionIncrement(1);
    }
    currentTokenPosition = t.position;
    return true;
  }

}

