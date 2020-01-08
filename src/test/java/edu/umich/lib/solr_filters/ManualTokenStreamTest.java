package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;

import java.io.IOException;

import static org.junit.Assert.*;

public class ManualTokenStreamTest {

  @Test
  public void singleAdd() throws IOException {
    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);
    assertTokenStreamContents(ts, new String[]{"Bill"});
  }

  @Test
  public void doubleAdd() throws IOException {
    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);
    ts.add("Dueber", 2);
    CharTermAttribute termAttribute = ts.addAttribute(CharTermAttribute.class);

    ts.incrementToken();
    assertEquals("Added first token", "Bill", termAttribute.toString());
    ts.incrementToken();
    assertEquals("Added second token", "Dueber", termAttribute.toString());
  }

  @Test
  public void addOverlapping() throws IOException {

    long advance = 1;
    long noAdvance = 0;

    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);
    ts.add("Düeber", 2);
    ts.add("Dueber", 2);
    ts.add("Danit", 3);

    ts.reset();
    ts.incrementToken();
    CharTermAttribute termAttribute = ts.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posAttribute = ts.addAttribute(PositionIncrementAttribute.class);

    assertEquals("First term correct", "Bill", termAttribute.toString());
    assertEquals("First position correct", advance, posAttribute.getPositionIncrement());

    ts.incrementToken();
    String t2a = termAttribute.toString();
    long posincr2a = posAttribute.getPositionIncrement();

    ts.incrementToken();
    String t2b = termAttribute.toString();
    long posincr2b = posAttribute.getPositionIncrement();

    assertEquals("Düeber", t2a);
    assertEquals("Dueber", t2b);
    assertEquals(advance, posincr2a);
    assertEquals(noAdvance, posincr2b);

    ts.incrementToken();
    String t3 = termAttribute.toString();
    long posincr3 = posAttribute.getPositionIncrement();

    assertEquals("Danit", t3);
    assertEquals(advance, posincr3);

  }

}
