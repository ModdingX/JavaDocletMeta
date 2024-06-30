package org.moddingx.java_doclet_meta.record.block;

import com.google.gson.JsonObject;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.util.DocTreePath;
import org.jetbrains.annotations.Nullable;
import org.moddingx.java_doclet_meta.DocEnv;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public record ClassTextBlock(Type type, String cls, String text) implements DocBlockData {

    @Override
    public void addProperties(JsonObject json) {
        json.addProperty("cls", cls);
        json.addProperty("text", text);
    }

    public static DocBlockData from(DocEnv env, Type type, @Nullable DocTreePath basePath, ReferenceTree ref, String text) {
        DocTreePath path = basePath == null ? null : DocTreePath.getPath(basePath, ref);
        Element element = path == null ? null : env.docs().getElement(path);
        if (element != null && (element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement t) {
            return new ClassTextBlock(type, env.elements().getBinaryName(t).toString().replace('.', '/'), text);
        } else {
            return new TextBlock(Type.UNKNOWN, ref.getSignature() + " " + text);
        }
    }
}
