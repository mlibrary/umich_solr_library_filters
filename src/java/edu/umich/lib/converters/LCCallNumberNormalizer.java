/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umich.lib.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.*;
import java.util.ArrayList;


/**
 * @author dueberb
 */
public class LCCallNumberNormalizer {

    public static final Long MINNUM = Long.MIN_VALUE;
    public static final String JOIN = "";
    public static final String TOPALPHA = "@@@@";
    public static final String TOPSPACES = "@@@@@@@@@@";
    public static final String TOPSPACE = "@";
    public static final String TOPDIGIT = "0";
    public static final String TOPDIGITS = "0000000000000000";
    public static final String BOTTOMSPACES = "~~~~~~~~~~~~~~";
    public static final String BOTTOMSPACE = "~";
    public static final String BOTTOMDIGIT = "9";
    public static final String BOTTOMDIGITS = "999999999999999999999";


    public static final int max_alpha_characters = 3;
    public static final int max_initial_digits = 6;
    public static final int max_integer_digits = 5;
    public static final int max_fractional_digits = 5;


    // I *should* build this with the immediately-above constants, but, man
    // regex in java is ugly.
    private static Pattern lcpattern = Pattern.compile(
            "^ \\s* (?:VIDEO-D)? (?:DVD-ROM)? (?:CD-ROM)? (?:TAPE-C)? \\s* " +
                    "([A-Z]{1,3}) \\s* " +
                    "(?: (\\d{1,6}) (?:\\.(\\d{1,6}))? )? \\s* " +

                    "(?: \\.? \\s* ([A-Z]) \\s* (\\d{1,5})? (?:\\.(\\d{1,5}))? )? \\s* " + // cutters
                    "(?: \\.? \\s* ([A-Z]) \\s* (\\d{1,5})? (?:\\.(\\d{1,5}))? )? \\s* " + // cutters
                    "(?: \\.? \\s* ([A-Z]) \\s* (\\d{1,5})? (?:\\.(\\d{1,5}))? )? \\s* " + // cutters
                    "(\\s\\s*(.+)?)? \\s*$",
            Pattern.COMMENTS);
    private static Pattern longAlphaPattern = Pattern.compile("^[A-Z]{4,}.*$");


    private static final Logger LOGGER = LoggerFactory
            .getLogger(LCCallNumberNormalizer.class);


    public static Boolean match(String s) {
        s = s.toUpperCase();

        Matcher m = lcpattern.matcher(s);
        return m.matches();
    }

    public static String normalize(String s) throws MalformedCallNumberException {
        s = s.toUpperCase();
        Matcher m = lcpattern.matcher(s);
        if (!m.matches()) {
            throw new MalformedCallNumberException();
        }

        String alpha = m.group(1);
        String num = m.group(2);
        String dec = m.group(3);
        String c1alpha = m.group(4);
        String c1num = m.group(5);
        String c1decimal = m.group(6);
        String c2alpha = m.group(7);
        String c2num = m.group(8);
        String c2decimal = m.group(9);
        String c3alpha = m.group(10);
        String c3num = m.group(11);
        String c3decimal = m.group(12);

        String extra = m.group(13);

        // If we don't have at least an alpha and a num, throw it out
        if (alpha == null || num == null) {
            throw new MalformedCallNumberException();
        }

        // If it's just too damn long, throw an exception
        Matcher lap = longAlphaPattern.matcher(s);
        if (lap.matches()) {
            throw new MalformedCallNumberException();
        }

        // Create a normalized version of the "extra" with a leading space
        String enorm = extra == null ? null : extra.replaceAll("[^A-Z0-9]", "");

        // For each component, normalize by padding them out
        // and add to a holding bit.

        ArrayList<String> normalized = new ArrayList<String>();
        push_if_not_null(normalized, (right_pad(alpha, max_alpha_characters, "@")));
        push_if_not_null(normalized, (left_pad(num, max_initial_digits, "0")));
        push_if_not_null(normalized, (right_pad(dec, max_fractional_digits, "0")));

        push_if_not_null(normalized, c1alpha);
        push_if_not_null(normalized, (right_pad(c1num, max_integer_digits, "0")));
        push_if_not_null(normalized, (right_pad(c1decimal, max_fractional_digits, "0")));

        push_if_not_null(normalized, c2alpha);
        push_if_not_null(normalized, (right_pad(c2num, max_integer_digits, "0")));
        push_if_not_null(normalized, (right_pad(c2decimal, max_fractional_digits, "0")));

        push_if_not_null(normalized, c3alpha);
        push_if_not_null(normalized, (right_pad(c3num, max_integer_digits, "0")));
        push_if_not_null(normalized, (right_pad(c3decimal, max_fractional_digits, "0")));

        push_if_not_null(normalized, enorm);

        return String.join("@", normalized);

    }

    private static void push_if_not_null(ArrayList arr, Object value) {
        if (value != null) {
            arr.add(value);
        }
    }

    private static StringBuilder repeatedString(String s, int times) {
        StringBuilder rv = new StringBuilder();
        for (int i = 0; i < times; i++) {
            rv.append(s);
        }
        return rv;
    }

    private static String right_pad(String s, int max, String replacementChar) {
        String template = repeatedString(replacementChar, max).toString();
        if (s == null) {
            return null;
        } else {
            return s + template.substring(0, max - s.length());
        }
    }

    private static String left_pad(String s, Integer max, String replacementChar) {
        String template = repeatedString(replacementChar, max).toString();

        if (s == null) {
            return null;
        } else {
            return template.substring(0, max - s.length()) + s;
        }
    }



}
