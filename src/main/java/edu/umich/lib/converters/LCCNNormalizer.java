package edu.umich.lib.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes Library of Congress Control Numbers (LCCNs) to canonical form as defined
 * by the <a href="http://www.loc.gov/marc/lccn-namespace.html#syntax">LC LCCN namespace
 * syntax</a>.
 *
 * <p>Normalization steps:
 * <ol>
 *   <li>Remove all whitespace and lowercase the string.</li>
 *   <li>Strip everything from the first {@code /} onward (revision qualifiers).</li>
 *   <li>If the result matches the LCCN pattern (optional alphabetic prefix followed by
 *       an optional hyphen and digit serial), zero-pad the serial to six digits.</li>
 *   <li>Return the prefix + padded serial, or just the prefix if no hyphen was present.</li>
 * </ol>
 */
public class LCCNNormalizer {

    /** Strips everything from the first slash onward (e.g. {@code /AC/r95} qualifiers). */
    private static final Pattern TRAILING_SLASH_PATTERN = Pattern.compile("^(.*?)/.*$");

    /**
     * Matches the LCCN structure: an optional alphabetic prefix followed by optional
     * {@code -digits}. Captures (1) the prefix and (2) the post-hyphen digits.
     */
    private static final Pattern LCCN_DASH_PATTERN = Pattern.compile("^(\\w+)(?:-(\\d+))?.*$");

    /**
     * Normalizes a raw LCCN string to canonical form.
     *
     * @param raw a raw LCCN string (may contain whitespace, hyphens, slash qualifiers)
     * @return the normalized LCCN, or the cleaned input as-is if it is not a recognizable LCCN
     */
    public static String normalize(String raw) {

        // Remove all whitespace and lowercase
        raw = raw.replaceAll("\\s+", "").toLowerCase();

        // Strip everything from the first slash onward
        Matcher m = TRAILING_SLASH_PATTERN.matcher(raw);
        if (m.matches()) {
            raw = m.group(1);
        }

        // Check viability; return as-is if not a recognizable LCCN structure
        m = LCCN_DASH_PATTERN.matcher(raw);
        if (!m.matches()) {
            return raw;
        }

        String prefix = m.group(1);
        String postHyphenDigits = m.group(2);

        // No hyphen present — return the prefix alone
        if (postHyphenDigits == null || postHyphenDigits.isEmpty()) {
            return prefix;
        }

        // Zero-pad the serial to six digits if necessary
        if (postHyphenDigits.length() < 6) {
            postHyphenDigits = String.format("%06d", Integer.parseInt(postHyphenDigits));
        }
        return prefix + postHyphenDigits;
    }
}
