package org.moddingx.java_doclet_meta.util;

import java.util.Map;

public class HtmlQuote {
    
    private static final Map<Integer, String> SPECIAL_QUOTES = Map.of(
            (int) '\r', "",
            (int) '<', "&lt;",
            (int) '>', "&gt;",
            (int) '&', "&amp;",
            (int) '"', "&quot;"
    );
    
    public static String quote(String str) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasNewline = false;
        for (int cp : str.codePoints().toArray()) {
            if (SPECIAL_QUOTES.containsKey(cp)) {
                sb.append(SPECIAL_QUOTES.get(cp));
            } else if (cp == '\n') {
                if (!lastWasNewline) {
                    sb.append(" ");
                } else {
                    sb.append("<br>");
                }
                lastWasNewline = !lastWasNewline;
            } else if (cp > 127 || Character.isISOControl(cp)) {
                sb.append("&#").append(cp).append(";");
            } else {
                sb.appendCodePoint(cp);
            }
        }
        return sb.toString();
    }
}
