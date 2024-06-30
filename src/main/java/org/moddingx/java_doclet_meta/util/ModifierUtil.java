package org.moddingx.java_doclet_meta.util;

import java.util.Comparator;
import java.util.Objects;

public class ModifierUtil {
    
    public static final Comparator<String> MODIFIER_ORDER = (s1, s2) -> Objects.equals(s1, s2) ? 0 : Integer.compare(getModIdx(s1), getModIdx(s2));

    private static int getModIdx(String str) {
        return switch (str) {
            case "public", "protected", "private" -> 0;
            case "static", "default" -> 1;
            case "abstract" -> 2;
            case "final", "sealed", "non-sealed" -> 3;
            case "native" -> 4;
            case "synchronized" -> 5;
            case "strictfp" -> 6;
            case "transient" -> 7;
            case "volatile" -> 8;
            case null, default -> 9;
        };
    }
}
