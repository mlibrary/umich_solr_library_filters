package edu.umich.lib.converters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ISBNNormalizerTest {

    @Test
    public void testExtractISBN13() throws Exception {
        assertEquals("9780802088512", ISBNNormalizer.extract_isbn13("978-080-208-8-51-2  "));
    }

    @Test
    public void testNormalize() throws Exception {
        String expResult = "9780802088512";
        assertEquals(expResult, ISBNNormalizer.normalize("0802088511"));
        assertEquals(expResult, ISBNNormalizer.normalize("9780802088512"), "Raw 13");
        assertEquals(expResult, ISBNNormalizer.normalize("978-080-208-8-51-2  "), "With dashes");
        assertEquals(expResult, ISBNNormalizer.normalize("080-2088-511"), "From 10-char");
        assertEquals(expResult, ISBNNormalizer.normalize("ISBN: 0802088511"), "with leading text");
        assertEquals(expResult, ISBNNormalizer.normalize("ISBN: 080-208-8511 (pb)"), "With leading/trailing text");
        assertEquals(expResult, ISBNNormalizer.normalize("Long ISBN: 978-080-208-8-51-2  "), "Very long");
    }

    @Test
    public void testProblemFromPrinceton() throws Exception {
        assertEquals("9791032000373", ISBNNormalizer.normalize("9791032000373"),
                "Problem from https://github.com/pulibrary/pul_solr/pull/3");
    }

    @Test
    public void testNonISBNs() {
        String[] nonISBN = {
                "12345-678 too short",
                "123-456-789-10 too long",
                "9770802088512 doesn't start with 978",
                "123-34-6789Y illegal checkdigit"
        };
        for (String str : nonISBN) {
            assertThrows(IllegalArgumentException.class,
                    () -> ISBNNormalizer.normalize(str),
                    "Expected IllegalArgumentException for: " + str);
        }
    }
}
