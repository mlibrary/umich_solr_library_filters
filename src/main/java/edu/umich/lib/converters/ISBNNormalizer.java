package edu.umich.lib.converters;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes ISBN strings to canonical ISBN-13 format.
 *
 * <p>Given a raw string that may contain an ISBN (possibly with hyphens, surrounding
 * text, or other noise), {@link #normalize(String)} returns a clean 13-digit ISBN-13
 * string. ISBN-10 inputs are converted to ISBN-13 by prepending "978" and recalculating
 * the check digit.
 */
public class ISBNNormalizer {

    private static final String ISBN_DELIMITER_PATTERN = "\\-";

    /** Pattern matching a candidate ISBN-10 within a larger string. */
    public static final Pattern ISBN10Pat =
            Pattern.compile("^.*?(\\d[\\d\\-]{8,}[Xx]?)(?:\\D|\\Z).*$");

    /** Pattern matching a candidate ISBN-13 within a larger string. */
    public static final Pattern ISBN13Pat =
            Pattern.compile("^.*?(97[89][\\d\\-]{10,})(?:\\D|\\Z).*$");

    /**
     * Extracts and normalizes an ISBN from the given string.
     *
     * <p>ISBN-13s are returned as-is (after stripping hyphens). ISBN-10s are converted
     * to ISBN-13. If no recognizable ISBN is found, an {@link IllegalArgumentException}
     * is thrown.
     *
     * @param isbnstring a string that may contain an ISBN
     * @return a normalized 13-digit ISBN-13 string
     * @throws IllegalArgumentException if no ISBN can be extracted
     */
    public static String normalize(String isbnstring) throws IllegalArgumentException {
        // Prefer ISBN-13; fall back to ISBN-10 → ISBN-13 conversion
        try {
            return extract_isbn13(isbnstring);
        } catch (IllegalArgumentException e) {
            return isbn10_to_13(extract_isbn10(isbnstring));
        }
    }

    /**
     * Extracts a raw ISBN digit string from {@code isbnstring} using the given pattern.
     *
     * @param isbnstring a string that may contain an ISBN
     * @param pat        the regex pattern to match (either {@link #ISBN10Pat} or {@link #ISBN13Pat})
     * @param len        the expected digit length (10 or 13)
     * @return the extracted ISBN digits (hyphens removed)
     * @throws IllegalArgumentException if no match is found or the extracted digits are the wrong length
     */
    public static String extract_isbn_by_pat(String isbnstring, Pattern pat, Integer len) throws IllegalArgumentException {
        Matcher m = pat.matcher(isbnstring);
        if (!m.matches()) {
            throw new IllegalArgumentException(isbnstring + " doesn't contain an ISBN" + len);
        }

        String extracted_string = m.group(1);
        String normalized_string = extracted_string.replaceAll(ISBN_DELIMITER_PATTERN, "");

        if (normalized_string.length() != len) {
            throw new IllegalArgumentException("'" + normalized_string + "' doesn't contain an ISBN" + len + "; its length is " + normalized_string.length());
        }
        return normalized_string;
    }

    /**
     * Extracts a 10-digit ISBN from {@code isbnstring}.
     *
     * @param isbnstring a string that may contain an ISBN-10
     * @return the extracted 10-digit ISBN (hyphens removed)
     * @throws IllegalArgumentException if no ISBN-10 is found
     */
    public static String extract_isbn10(String isbnstring) throws IllegalArgumentException {
        return extract_isbn_by_pat(isbnstring, ISBN10Pat, 10);
    }

    /**
     * Extracts a 13-digit ISBN from {@code isbnstring}.
     *
     * @param isbnstring a string that may contain an ISBN-13
     * @return the extracted 13-digit ISBN (hyphens removed)
     * @throws IllegalArgumentException if no ISBN-13 is found
     */
    public static String extract_isbn13(String isbnstring) throws IllegalArgumentException {
        return extract_isbn_by_pat(isbnstring, ISBN13Pat, 13);
    }

    /**
     * Converts an already-extracted ISBN-10 to ISBN-13.
     *
     * <p>The input must be exactly 10 characters: 9 digits followed by a digit or 'X'
     * check character. The "978" prefix is prepended and a new EAN-13 check digit is
     * calculated.
     *
     * @param isbn10 the raw 10-character ISBN-10 (no hyphens)
     * @return the equivalent 13-digit ISBN-13 string
     */
    public static String isbn10_to_13(String isbn10) {
        String longisbn = "978" + isbn10.substring(0, 9);

        int[] digits = new int[12];
        for (int i = 0; i < 12; i++) {
            digits[i] = Integer.parseInt(longisbn.substring(i, i + 1));
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum = sum + digits[i] + (2 * digits[i] * (i % 2));
        }

        // Check digit is the amount needed to reach the next multiple of 10
        int check = 10 - (sum % 10);
        if (check == 10) {
            return longisbn + "0";
        } else {
            return longisbn + check;
        }
    }
}
