package io.github.noeppi_noeppi.tools.java_doclet_meta.record;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.JsonUtil;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.ModifierUtil;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record ClassData(
        String simpleName,
        String sourceName,
        String binaryName,
        List<String> modifiers,
        NestingKind nesting,
        Optional<TypeData> superClass,
        List<TypeData> interfaces,
        List<String> enumValues,
        List<FieldData> fields,
        List<MethodData> constructors,
        List<MethodData> methods,
        Optional<DocData> doc
) {

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("name", binaryName);
        json.addProperty("simpleName", simpleName);
        json.addProperty("sourceName", sourceName);
        json.add("modifiers", JsonUtil.array(modifiers));
        if (nesting != NestingKind.TOP_LEVEL) json.addProperty("nesting", nesting.name().toLowerCase(Locale.ROOT));
        superClass.ifPresent(s -> json.add("superClass", s.json()));
        if (!interfaces.isEmpty()) json.add("interfaces", JsonUtil.array(interfaces, TypeData::json));
        if (!enumValues.isEmpty()) json.add("enumValues", JsonUtil.array(enumValues));
        if (!fields.isEmpty()) json.add("fields", JsonUtil.array(fields, FieldData::json));
        if (!constructors.isEmpty()) json.add("constructors", JsonUtil.array(constructors, MethodData::json));
        if (!methods.isEmpty()) json.add("methods", JsonUtil.array(methods, MethodData::json));
        doc.ifPresent(d -> json.add("doc", d.json()));
        return json;
    }
    
    public static ClassData from(DocEnv env, TypeElement element) {
        String simpleName = element.getSimpleName().toString();
        String sourceName = element.getQualifiedName().toString();
        String binaryName = env.elements().getBinaryName(element).toString().replace('.', '/');
        List<String> modifiers = element.getModifiers().stream().map(Modifier::toString).sorted(ModifierUtil.MODIFIER_ORDER).toList();
        NestingKind nesting = element.getNestingKind();
        Optional<TypeData> superClass = superClassName(env, element);
        List<TypeData> interfaces = element.getInterfaces().stream().flatMap(itf -> TypeData.from(env, itf).stream()).toList();

        ArrayList<String> enumConstants = new ArrayList<>();
        ArrayList<FieldData> fields = new ArrayList<>();
        ArrayList<MethodData> constructors = new ArrayList<>();
        ArrayList<MethodData> methods = new ArrayList<>();
        
        for (Element elem : element.getEnclosedElements()) {
            switch (elem.getKind()) {
                case ENUM_CONSTANT -> enumConstants.add(elem.getSimpleName().toString());
                case FIELD -> fields.add(FieldData.from(env, (VariableElement) elem));
                case CONSTRUCTOR -> constructors.add(MethodData.fromUnnamed(env, (ExecutableElement) elem));
                case METHOD -> methods.add(MethodData.from(env, (ExecutableElement) elem));
            }
        }
        
        Optional<DocData> doc = DocData.from(env, element);
        
        return new ClassData(
                simpleName, sourceName, binaryName, modifiers, nesting, superClass, interfaces,
                element.getKind() == ElementKind.ENUM ? List.copyOf(enumConstants) : List.of(),
                List.copyOf(fields), List.copyOf(constructors), List.copyOf(methods), doc
        );
    }
    
    private static Optional<TypeData> superClassName(DocEnv env, TypeElement element) {
        Optional<TypeData> result = TypeData.from(env, element.getSuperclass());
        if (result.isPresent()) return result;
        if (env.elements().getBinaryName(element).toString().equals("java.lang.Object")) return Optional.empty();
        return Optional.of(TypeData.ROOT);
    }
    
     
}
