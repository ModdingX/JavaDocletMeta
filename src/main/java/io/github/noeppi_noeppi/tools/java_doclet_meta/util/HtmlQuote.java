package io.github.noeppi_noeppi.tools.java_doclet_meta.util;

import java.util.Map;

public class HtmlQuote {
    
    private static final Map<Integer, String> SPECIAL_QUOTES = Map.of(
            (int) '\n', "<br>",
            (int) '\r', "",
            (int) '<', "&lt;",
            (int) '>', "&gt;",
            (int) '&', "&amp;",
            (int) '"', "&quot;"
    );
    
    public static String quote(String str) {
        StringBuilder sb = new StringBuilder();
        str.codePoints().forEach(cp -> {
            if (SPECIAL_QUOTES.containsKey(cp)) {
                sb.append(SPECIAL_QUOTES.get(cp));
            } else if (cp > 127 || Character.isISOControl(cp)) {
                sb.append("&#").append(cp).append(";");
            } else {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }
}
