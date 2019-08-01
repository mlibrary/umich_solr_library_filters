package edu.umich.lib.solr_filters;

import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;


import java.util.*;

import static org.junit.Assert.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class AnchoredSearchFilterTest {


    @Test
    public void testJoinedStrings() {
        Map<Integer, List<String>> hm = new HashMap<>();
        String[] p1 = {"Bill", "bill"};
        String[] p2 = {"Dueber", "dueber"};
        List<String> pos0 = Arrays.asList("Bill", "bill");
        List<String> pos1 = Arrays.asList("Dueber", "dueber");
        hm.put(0, pos0);
        hm.put(1, pos1);

        ArrayList<String> expected = new ArrayList<>();
        expected.add("Bill_Dueber_");
        expected.add("Bill_dueber_");
        expected.add("bill_Dueber_");
        expected.add("bill_dueber_");

        AnchoredSearchFilter asf = new AnchoredSearchFilter(new StandardTokenizer());

        assertEquals(expected, asf.joinedStrings(0, 1, hm));

    }
}