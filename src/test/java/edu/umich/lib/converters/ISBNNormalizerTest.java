package edu.umich.lib.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class ISBNNormalizerTest {

    @org.junit.Test
    public void testExtractISBN13() throws Exception {
        assertEquals("9780802088512", ISBNNormalizer.extract_isbn13("978-080-208-8-51-2  "));
    }

    @org.junit.Test
    public void testNormalize() throws Exception {

        String isbn = "0802088511";
        String expResult = "9780802088512";
        String result = ISBNNormalizer.normalize(isbn);
        assertEquals(expResult, result);
        assertEquals("Raw 13", expResult, ISBNNormalizer.normalize("9780802088512"));
        assertEquals("With dashes", expResult, ISBNNormalizer.normalize("978-080-208-8-51-2  "));
        assertEquals("From 10-char", expResult, ISBNNormalizer.normalize("080-2088-511"));
        assertEquals("with leading text", expResult, ISBNNormalizer.normalize("ISBN: 0802088511"));
        assertEquals("With leading/trailing text", expResult, ISBNNormalizer.normalize("ISBN: 080-208-8511 (pb)"));
        assertEquals("Very long", expResult, ISBNNormalizer.normalize("Long ISBN: 978-080-208-8-51-2  "));
    }
    
    @org.junit.Test
    public void testProblemFromPrinceton() throws Exception {
      assertEquals("Problem from https://github.com/pulibrary/pul_solr/pull/3", "9791032000373", ISBNNormalizer.normalize("9791032000373"));
    }

    @org.junit.Test
    public void testNonISBNs() throws Exception {
        String[] nonISBN = {
                "12345-678 too short",
                "123-456-789-10 too long",
                "9770802088512 doesn't start with 978",
                "123-34-6789Y illegal checkdigit"

        };
        for (String str : nonISBN) {
            try {
                ISBNNormalizer.normalize(str);
                fail("Failure: '" + str + "' is not an ISBN\\n");
            } catch (IllegalArgumentException e) {
                // all is well
            }
        }
    }
}
