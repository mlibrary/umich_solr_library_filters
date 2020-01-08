package edu.umich.lib.solr_filters;


import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;

public class AnchoredSearchFilterTest {

  @Test
  public void testJoinedStrings() {
    Map<Integer, List<String>> hm = new HashMap<>();
    String[] p1 = {"Bill", "bill"};
    String[] p2 = {"Dueber", "dueber"};
    List<String> pos1 = Arrays.asList("Bill", "bill");
    List<String> pos2 = Arrays.asList("Dueber", "dueber");
    hm.put(1, pos1);
    hm.put(2, pos2);

    ArrayList<String> expected = new ArrayList<>();
    expected.add("Bill_Dueber");
    expected.add("Bill_dueber");
    expected.add("bill_Dueber");
    expected.add("bill_dueber");

    AnchoredSearchFilter asf = new AnchoredSearchFilter();

    assertEquals("Joining tokens works", expected, asf.joinedTokens(hm));

  }

  @Test
  public void testAnchors() throws IOException {
    ManualTokenStream ts = new ManualTokenStream();
    ts.add("Bill", 1);
    ts.add("Düeber", 2);
    ts.add("Dueber", 2);
    ts.add("Danit", 3);

    AnchoredSearchFilter asf = new AnchoredSearchFilter(ts);
    CharTermAttribute termAttribute = asf.addAttribute(CharTermAttribute.class);

    asf.incrementToken();
    assertEquals("Bill_Düeber_Danit", termAttribute.toString());

    asf.incrementToken();
    assertEquals("Bill_Dueber_Danit", termAttribute.toString());

    assertFalse(asf.incrementToken());
  }
}
