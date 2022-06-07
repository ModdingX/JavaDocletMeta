package org.moddingx.java_doclet_meta.record;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.moddingx.java_doclet_meta.DocEnv;
import org.moddingx.java_doclet_meta.util.JsonUtil;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public record TypeData(
        String name,
        String signature,
        List<Optional<TypeData>> parameters
) {
    
    public static final TypeData ROOT = new TypeData("java/lang/Object", "java.lang.Object", List.of());

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("signature", signature);
        json.add("parameters", JsonUtil.array(parameters, p -> p.isPresent() ? p.get().json() : JsonNull.INSTANCE));
        return json;
    }
    
    public static Optional<TypeData> from(DocEnv env, TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED && type instanceof DeclaredType dt) {
            Element element = dt.asElement();
            if ((element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement te) {
                return Optional.of(new TypeData(
                        env.elements().getBinaryName(te).toString().replace('.', '/'),
                        type.toString(), dt.getTypeArguments().stream().map(p -> from(env, p)).toList()
                ));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
