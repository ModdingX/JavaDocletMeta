package io.github.noeppi_noeppi.tools.java_doclet_meta.record;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.JsonUtil;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.ModifierUtil;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import java.util.List;
import java.util.Optional;

public record MethodData(
        Optional<String> name,
        List<String> modifiers,
        String type,
        List<ParamData> parameters,
        DescriptorData returnType,
        boolean vararg,
        List<DescriptorData> thrownTypes,
        Optional<DocData> doc
) {

    public JsonObject json() {
        JsonObject json = new JsonObject();
        name.ifPresent(s -> json.addProperty("name", s));
        json.add("modifiers", JsonUtil.array(modifiers));
        json.addProperty("type", type);
        json.add("parameters", JsonUtil.array(parameters, ParamData::json));
        json.add("return", returnType.json());
        if (vararg) json.addProperty("vararg", true);
        json.add("throws", JsonUtil.array(thrownTypes, DescriptorData::json));
        doc.ifPresent(d -> json.add("doc", d.json()));
        return json;
    }
    
    public static MethodData from(DocEnv env, ExecutableElement element) {
        return from(Optional.of(element.getSimpleName().toString()), env, element);
    }
    
    public static MethodData fromRecord(DocEnv env, RecordComponentElement element) {
        return from(Optional.of(element.getSimpleName().toString()), env, element.getAccessor());
    }
    
    public static MethodData fromUnnamed(DocEnv env, ExecutableElement element) {
        return from(Optional.empty(), env, element);
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static MethodData from(Optional<String> name, DocEnv env, ExecutableElement element) {
        List<String> modifiers = element.getModifiers().stream().map(Modifier::toString).sorted(ModifierUtil.MODIFIER_ORDER).toList();
        String type = element.asType().toString();
        List<ParamData> parameters = element.getParameters().stream().map(p -> ParamData.from(env, p)).toList();
        DescriptorData returnType = DescriptorData.from(env, element.getReturnType());
        boolean vararg = element.isVarArgs();
        List<DescriptorData> thrownTypes = element.getThrownTypes().stream().map(t -> DescriptorData.from(env, t)).toList();
        Optional<DocData> doc = DocData.from(env, element);
        return new MethodData(name, modifiers, type, parameters, returnType, vararg, thrownTypes, doc);
    }
}
