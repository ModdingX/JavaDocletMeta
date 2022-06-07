package org.moddingx.java_doclet_meta.record.block;

import com.google.gson.JsonObject;

public record TextBlock(Type type, String text) implements DocBlockData {

    @Override
    public void addProperties(JsonObject json) {
        json.addProperty("text", text);
    }
}
