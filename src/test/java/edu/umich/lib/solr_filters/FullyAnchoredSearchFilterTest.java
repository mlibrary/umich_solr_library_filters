package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.Test;
import java.io.IOException;

public class FullyAnchoredSearchFilterTest extends BaseTokenStreamTestCase {

  @Test
  public void test1() throws IOException {
    TokenStream input = whitespaceMockTokenizer("one two three");
    FullyAnchoredSearchFilter fasf = new FullyAnchoredSearchFilter(input);
    assertTokenStreamContents(fasf, new String[] { "one1", "two2", "three000"});
  }
}