package edu.umich.lib.solr_filters;

import junit.framework.TestCase;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;

public class LeftAnchoredSearchFilterTest extends TestCase {


  public void testOneToken() throws IOException {
    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);

    LeftAnchoredSearchFilter lasf = new LeftAnchoredSearchFilter(ts);
    CharTermAttribute termAttribute = lasf.addAttribute(CharTermAttribute.class);

    lasf.incrementToken();
    assertEquals("Bill1",termAttribute.toString() );

  }

  public void testMultipleTokens() throws IOException {
    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);
    ts.add("John", 2);
    ts.add("James", 2);
    ts.add("Dueber", 3);

    LeftAnchoredSearchFilter lasf = new LeftAnchoredSearchFilter(ts);
    CharTermAttribute termAttribute = lasf.addAttribute(CharTermAttribute.class);

    lasf.incrementToken();
    assertEquals("Bill1",termAttribute.toString() );

    lasf.incrementToken();
    assertEquals("John2",termAttribute.toString() );

    lasf.incrementToken();
    assertEquals("James2",termAttribute.toString() );


    lasf.incrementToken();
    assertEquals("Dueber3",termAttribute.toString() );

  }
}
