package org.moddingx.java_doclet_meta.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.function.Function;

public class JsonUtil {
    
    public static JsonArray array(List<String> elements) {
        JsonArray array = new JsonArray();
        elements.forEach(array::add);
        return array;
    }

    public static <T> JsonArray array(List<T> elements, Function<T, ? extends JsonElement> mapper) {
        JsonArray array = new JsonArray();
        elements.forEach(e -> array.add(mapper.apply(e)));
        return array;
    }
}
