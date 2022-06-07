package org.moddingx.java_doclet_meta.record;

import com.google.gson.JsonObject;
import org.moddingx.java_doclet_meta.DocEnv;
import org.moddingx.java_doclet_meta.util.JsonUtil;
import org.moddingx.java_doclet_meta.util.ModifierUtil;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

public record FieldData(
        String name,
        List<String> modifiers,
        DescriptorData type,
        Optional<Object> constant,
        Optional<DocData> doc
) {

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.add("modifiers", JsonUtil.array(modifiers));
        json.add("type", type.json());
        constant.ifPresent(o -> {
            if (o instanceof String str) {
                json.addProperty("constant", str);
            } else if (o instanceof Number n) {
                json.addProperty("constant", n);
            } else if (o instanceof Boolean b) {
                json.addProperty("constant", b);
            } else if (o instanceof Character c) {
                json.addProperty("constant", c);
            }
        });
        doc.ifPresent(d -> json.add("doc", d.json()));
        return json;
    }
    
    public static FieldData from(DocEnv env, VariableElement element) {
        String name = element.getSimpleName().toString();
        List<String> modifiers = element.getModifiers().stream().map(Modifier::toString).sorted(ModifierUtil.MODIFIER_ORDER).toList();
        DescriptorData type = DescriptorData.from(env, element.asType());
        Optional<Object> constant = Optional.ofNullable(element.getConstantValue());
        Optional<DocData> doc = DocData.from(env, element);
        return new FieldData(name, modifiers, type, constant, doc);
    }
}
