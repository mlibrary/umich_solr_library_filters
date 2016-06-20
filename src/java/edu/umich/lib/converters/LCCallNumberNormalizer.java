/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umich.lib.normalizers;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author dueberb
 */
public class LCCallNumberNormalizer {

    public static final Long MINNUM = new Long(Long.MIN_VALUE);
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
    private static Pattern lcpattern = Pattern.compile(
        "^ \\s* (?:VIDEO-D)? (?:DVD-ROM)? (?:CD-ROM)? (?:TAPE-C)? \\s* ([A-Z]{1,3}) \\s* (?: (\\d{1,4}) (?:\\s*?\\.\\s*?(\\d{1,3}))? )? \\s* (?: \\.? \\s* ([A-Z]) \\s* (\\d{1,3} | \\Z)? )? \\s* (?: \\.? \\s* ([A-Z]) \\s* (\\d{1,3} | \\Z)? )? \\s* (?: \\.? \\s* ([A-Z]) \\s* (\\d{1,3} | \\Z)? )? (\\s\\s*.+?)? \\s*$",
        Pattern.COMMENTS);
    private static Pattern longAlphaPattern = Pattern.compile("^[A-Z]{4,}.*$");

    public static String join(List<String> s, String d) {
        StringBuffer rv = new StringBuffer("");
        ListIterator i = s.listIterator();
        if (!i.hasNext()) {
            return "";
        }
        rv.append(i.next());
        while (i.hasNext()) {
            rv.append(d);
            rv.append(i.next());
        }
        return rv.toString();
    }

    public static Boolean match(String s) {
        s = s.toUpperCase();

        Matcher m = lcpattern.matcher(s);
        if (m.matches()) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String normalize(String s) {
        try {
            return normalize(s, false, false);
        }
        catch (MalformedCallNumberException e) {
            return s.toUpperCase();
        }

    }

    public static String normalizeFullLength(String s) {

        try {
            return normalize(s, false, true);
        }
        catch (MalformedCallNumberException e) {
            return s.toUpperCase();
        }
    }

    public static String rangeStart(String s) {
        try {
            return normalize(s, false, false);
        }
        catch (MalformedCallNumberException e) {
            return s.toUpperCase();
        }
    }

    public static String rangeEnd(String s) {
        try {
            return normalize(s, false, false) + BOTTOMSPACE;
        }
        catch (MalformedCallNumberException e) {
            return s.toUpperCase();
        }
    }

    public static String rangeEndPadded(String s) {
        try {
            return normalize(s, true, true);
        }
        catch (MalformedCallNumberException e) {
            return s.toUpperCase();
        }
    }

    public static String normalize(String s, Boolean rangeEnd, Boolean padded)
        throws MalformedCallNumberException {
        s = s.toUpperCase();
//        System.out.println(s);
        Matcher m = lcpattern.matcher(s);
        if (!m.matches()) {
//            System.out.println(s + " does not match in normalize");
            throw new MalformedCallNumberException();
        }
//        System.out.println(s + " matches");

        String alpha = m.group(1);
        String num = m.group(2);
        String dec = m.group(3);
        String c1alpha = m.group(4);
        String c1num = m.group(5);
        String c2alpha = m.group(6);
        String c2num = m.group(7);
        String c3alpha = m.group(8);
        String c3num = m.group(9);
        String extra = m.group(10);

        // If we don't have at least an alpha and a num, throw it out
        if (alpha == null || num == null) {
            throw new MalformedCallNumberException();
        }

        Matcher lap = longAlphaPattern.matcher(s);
        if (lap.matches()) {
            throw new MalformedCallNumberException();

        }

        // Record the originals
        ArrayList<String> origs = new ArrayList<String>(10);
        for (int i = 1; i <= 10; i++) {
            origs.add(m.group(i));
        }

        //We have some records that aren't LoC Call Numbers, but start like them,
        //only with three digits in the decimal. Ditch them

        // if (dec != null && dec.length() > 3) {
        //             //  System.out.println(s + "has too long a decimal");
        //             throw new MalformedCallNumberException();
        //         }
        

        // Normalize each part and push them onto a stack

        // Create a normalized version of the "extra" with a leading space
        String enorm = extra == null ? "" : extra;
        enorm.replaceAll("[^A-Z0-9]", "");
        if (enorm.length() > 0) {
            enorm = " " + enorm;
        }

        // Pad the number out to four digits
        String orignum = num;
        String bottomnum = num;
        if (bottomnum == null) {
            bottomnum = "9999";
        }
        else {
            bottomnum = String.format("%04d", Integer.parseInt(bottomnum));
        }
        num = num == null || num.equals("") ? "0000" : String.format("%04d", Integer.parseInt(num));


        ArrayList<String> topnorm = new ArrayList<String>(10);
        topnorm.add(alpha + TOPALPHA.substring(0, 3 - alpha.length()));
        topnorm.add(num);
        topnorm.add(dec == null ? "000" : dec + TOPDIGITS.substring(0, 3 - dec.length()));
        topnorm.add(c1alpha == null ? TOPSPACE : c1alpha);
        topnorm.add(c1num == null ? "000" : c1num + TOPDIGITS.substring(0, 3 - c1num.length()));
        topnorm.add(c2alpha == null ? TOPSPACE : c2alpha);
        topnorm.add(c2num == null ? "000" : c2num + TOPDIGITS.substring(0, 3 - c2num.length()));
        topnorm.add(c3alpha == null ? TOPSPACE : c3alpha);
        topnorm.add(c3num == null ? "000" : c3num + TOPDIGITS.substring(0, 3 - c3num.length()));
        topnorm.add(enorm);


        //If we want a normalized, padded top, just return it
        if (padded && !rangeEnd) {
//            System.out.println(s + "is padded; returning");
            return join(topnorm, JOIN);
        }

        ArrayList<String> bottomnorm = new ArrayList<String>(10);
        if (rangeEnd) {
            bottomnorm.add(alpha + TOPALPHA.substring(0, 3 - alpha.length()));
            bottomnorm.add(bottomnum);
            bottomnorm.add(dec == null ? "999" : dec + BOTTOMDIGITS.substring(0, 3 - dec.length()));
            bottomnorm.add(c1alpha == null ? BOTTOMSPACE : c1alpha);
            bottomnorm.add(c1num == null ? "999" : c1num + BOTTOMDIGITS.substring(0, 3 - c1num.length()));
            bottomnorm.add(c2alpha == null ? BOTTOMSPACE : c2alpha);
            bottomnorm.add(c2num == null ? "999" : c2num + BOTTOMDIGITS.substring(0, 3 - c2num.length()));
            bottomnorm.add(c3alpha == null ? BOTTOMSPACE : c3alpha);
            bottomnorm.add(c3num == null ? "999" : c3num + BOTTOMDIGITS.substring(0, 3 - c3num.length()));
            bottomnorm.add(enorm);
        }



        // If we've got an alpha and nothing else, return it.
        // If we've got an alpha and nothing else but an 'extra', it's probably malformed.
        boolean hasAlpha = alpha != null;
        boolean hasExtra = extra != null;
        boolean hasOther = false;
        for (int i = 2; i <= 9; i++) {
            if (m.group(i) != null) {
                hasOther = true;
            }
        }

        if (hasAlpha && !hasOther) {
            if (hasExtra) {
//                System.out.println(s + " has alpha, but no other, and an extra");
                // Return identity
                throw new MalformedCallNumberException();
            }
            if (rangeEnd) {
                if (padded) {
                    return join(bottomnorm, JOIN);
                }
                else {
                    return alpha + BOTTOMSPACE;
                }
            }
            if (!padded) {
//                System.out.println(s + "has only alpha; returning " + alpha);
                return alpha;
            }
        }

        // If it's full-length already, just return it.
        if (extra != null) {
//            System.out.println(s + " is already full-length; returning topnorm");
            return join(topnorm, JOIN);
        }

        // Remove 'extra'
        topnorm.remove(9);
        if (rangeEnd) {
            bottomnorm.remove(9);
        }

        for (int i = 8; i >= 1; i--) {
            String end = topnorm.remove(i).toString(); // pop it off

            if (origs.get(i) != null) {
                if (rangeEnd) {
                    end = join(bottomnorm.subList(i, 9), JOIN);
                }
                else {
                    // get the original so we don't unnecssarily pad num or decimal
                    if (i > 1) {
                        end = origs.get(i);
                    }
                }
                String rv = join(topnorm, JOIN) + JOIN + end;
//                System.out.println("Standard return " + rv);
                return rv;

            }
        }
        return "Something went horribly wrong\n";
    }

    public static String toLongInt(String callno) {
        LCCallNumberNormalizerIntmap intmap = LCCallNumberNormalizerIntmap.getInstance();
        try {
            String n = normalize(callno, false, true);
        }
        catch (MalformedCallNumberException e) {
            return MINNUM.toString();
        }


        String n = normalizeFullLength(callno);

        // if n = callno, well, something is probably wrong. That would mean that a full-length
        // normalization is exactly the same as the input, which is hard to believe.

        // Get the first 18 characters, up through the second cutter's first two digits
        String alpha = n.substring(0, 3).toLowerCase().replace(TOPSPACE, "");
        String rest = n.substring(3, 17); // through the second cutter
//        System.out.println(callno + " / '" + alpha + "' | " + "'" +  rest + "'");

        String rv = intmap.get(alpha);
        if (rv == null) {
            return MINNUM.toString();
        }
//        System.out.print(rv + "\n  ");
        for (int i = 0; i < rest.length(); i++) {
            rv += intmap.get(String.valueOf(rest.charAt(i)));
//            System.out.print(intmap.get(String.valueOf(rest.charAt(i))));
        }
//        System.out.println();
        Long rvlong = new Long(rv);
        rvlong += MINNUM;
        return Long.toString(rvlong);


    }
}
