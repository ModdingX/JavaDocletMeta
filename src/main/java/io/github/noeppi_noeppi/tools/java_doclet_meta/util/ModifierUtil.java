package io.github.noeppi_noeppi.tools.java_doclet_meta.util;

import java.util.Comparator;
import java.util.Objects;

public class ModifierUtil {
    
    public static final Comparator<String> MODIFIER_ORDER = (s1, s2) -> Objects.equals(s1, s2) ? 0 : Integer.compare(getModIdx(s1), getModIdx(s2));

    private static int getModIdx(String str) {
        if ("public".equals(str) || "protected".equals(str) || "private".equals(str)) {
            return 0;
        } else if ("static".equals(str) || "default".equals(str)) {
            return 1;
        } else if ("native".equals(str) || "abstract".equals(str)) {
            return 2;
        } else if ("final".equals(str) || "sealed".equals(str) || "non-sealed".equals(str)) {
            return 3;
        } else if ("synchronized".equals(str)) {
            return 4;
        } else if ("strictfp".equals(str)) {
            return 5;
        } else if ("transient".equals(str)) {
            return 6;
        } else if ("volatile".equals(str)) {
            return 7;
        } else {
            return 100;
        }
    }
}
