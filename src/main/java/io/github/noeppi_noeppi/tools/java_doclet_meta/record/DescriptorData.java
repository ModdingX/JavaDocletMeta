package io.github.noeppi_noeppi.tools.java_doclet_meta.record;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public record DescriptorData(
        String name,
        Optional<String> desc,
        Optional<String> typeVar,
        Optional<String> binaryName,
        Optional<DescriptorData> arrayOf
) {

    public static final DescriptorData INVALID = new DescriptorData("", Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    
    public static final DescriptorData BOOLEAN = new DescriptorData("boolean", Optional.of("Z"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData BYTE = new DescriptorData("byte", Optional.of("B"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData SHORT = new DescriptorData("short", Optional.of("S"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData INTEGER = new DescriptorData("int", Optional.of("I"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData LONG = new DescriptorData("long", Optional.of("J"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData FLOAT = new DescriptorData("float", Optional.of("F"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData DOUBLE = new DescriptorData("double", Optional.of("D"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData CHARACTER = new DescriptorData("char", Optional.of("C"), Optional.empty(), Optional.empty(), Optional.empty());
    public static final DescriptorData VOID = new DescriptorData("void", Optional.of("V"), Optional.empty(), Optional.empty(), Optional.empty());
    
    public static final DescriptorData WILDCARD = new DescriptorData("?", Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        desc.ifPresent(s -> json.addProperty("desc", s));
        typeVar.ifPresent(s -> json.addProperty("typeVar", s));
        binaryName.ifPresent(s -> json.addProperty("binaryName", s));
        arrayOf.ifPresent(d -> json.add("arrayOf", d.json()));
        return json;
    }
    
    public static DescriptorData from(DocEnv env, TypeMirror type) {
        return switch (type.getKind()) {
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case SHORT -> SHORT;
            case INT -> INTEGER;
            case LONG -> LONG;
            case CHAR -> CHARACTER;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            case VOID -> VOID;
            case NONE, NULL, ERROR, PACKAGE, EXECUTABLE, OTHER, UNION, INTERSECTION, MODULE-> INVALID;
            case WILDCARD -> WILDCARD;
            case TYPEVAR -> new DescriptorData(type.toString(), Optional.empty(), Optional.of(type.toString()), Optional.empty(), Optional.empty());
            case ARRAY -> new DescriptorData(type.toString(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(from(env, ((ArrayType) type).getComponentType())));
            case DECLARED -> {
                Element element = ((DeclaredType) type).asElement();
                if ((element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement te) {
                    String binary = env.elements().getBinaryName(te).toString().replace('.', '/');
                    yield new DescriptorData(type.toString(), Optional.of("L" + binary + ";"), Optional.empty(), Optional.of(binary), Optional.empty());
                } else {
                    yield new DescriptorData(type.toString(), Optional.of("Ljava/lang/Object;"), Optional.empty(), Optional.empty(), Optional.empty());
                }
            }
        };
    }
}
