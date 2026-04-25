package group.austale.deitylandprotection;

/**
 * Tiny string-scanning helpers shared by classes that hand-parse our small
 * config / state JSON files. Not a real JSON parser: just enough to fish a
 * single named scalar value out of a flat object.
 *
 * <p>All readers return {@code null} (or {@code 0L} for {@link #readLong})
 * when the key is absent or unparseable, mirroring the original behaviour of
 * the duplicated copies in {@code DeityLandProtectionPlugin}, {@code
 * ClaimStore}, and {@code UpkeepStore}.</p>
 */
final class JsonReader {

    private JsonReader() {}

    /** Returns the index of the {@code :} that follows {@code "key"}, or {@code -1}. */
    static int findColonAfter(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return -1;
        }
        return obj.indexOf(':', k + pattern.length());
    }

    static int skipWhitespace(String s, int from) {
        int i = from;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    static String readString(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int firstQuote = obj.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = obj.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return obj.substring(firstQuote + 1, secondQuote);
    }

    static Integer readInt(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int i = skipWhitespace(obj, colon + 1);
        int j = scanNumber(obj, i);
        if (j == i) return null;
        try {
            return Integer.parseInt(obj, i, j, 10);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static long readLong(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return 0L;
        int i = skipWhitespace(obj, colon + 1);
        int j = scanNumber(obj, i);
        if (j == i) return 0L;
        try {
            return Long.parseLong(obj, i, j, 10);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    static Boolean readBoolean(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int i = skipWhitespace(obj, colon + 1);
        if (i >= obj.length()) return null;
        if (obj.startsWith("true", i)) return Boolean.TRUE;
        if (obj.startsWith("false", i)) return Boolean.FALSE;
        return null;
    }

    private static int scanNumber(String s, int from) {
        int j = from;
        while (j < s.length() && (s.charAt(j) == '-' || Character.isDigit(s.charAt(j)))) {
            j++;
        }
        return j;
    }
}
