package org.moddingx.java_doclet_meta;

import com.google.gson.JsonObject;
import org.moddingx.java_doclet_meta.record.ClassData;
import org.moddingx.java_doclet_meta.record.FieldData;
import org.moddingx.java_doclet_meta.util.JsonUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DocIndex {
    
    private final Map<String, String> classes = new HashMap<>();
    private final Map<String, Set<String>> members = new HashMap<>();
    
    public void add(ClassData cls) {
        classes.put(cls.sourceName(), cls.binaryName() + ".json");
        cls.enumValues().forEach(v -> addMember(cls, v));
        cls.fields().stream().map(FieldData::name).forEach(v -> addMember(cls, v));
        cls.methods().stream().flatMap(d -> d.name().stream()).forEach(v -> addMember(cls, v));
    }
    
    private void addMember(ClassData cls, String member) {
        members.computeIfAbsent(member, k -> new HashSet<>()).add(cls.binaryName() + ".json");
    }
    
    public JsonObject json() {
        JsonObject c = new JsonObject();
        classes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> c.addProperty(e.getKey(), e.getValue()));
        
        JsonObject m = new JsonObject();
        members.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> m.add(e.getKey(), JsonUtil.array(e.getValue().stream().sorted().toList())));
        
        JsonObject json = new JsonObject();
        json.add("classes", c);
        json.add("members", m);
        return json;
    }
}
