package io.github.noeppi_noeppi.tools.java_doclet_meta.record;

import com.google.gson.JsonObject;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.HtmlConverter;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.Optional;

public record ParamData(
        String name,
        DescriptorData type,
        Optional<String> doc
) {

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.add("type", type.json());
        doc.ifPresent(s -> json.addProperty("doc", s));
        return json;
    }
    
    public static ParamData from(DocEnv env, VariableElement element) {
        String name = element.getSimpleName().toString();
        DescriptorData type = DescriptorData.from(env, element.asType());
        Optional<String> doc = getParamDoc(env, name, element.getEnclosingElement());
        return new ParamData(name, type, doc);
    }
    
    private static Optional<String> getParamDoc(DocEnv env, String name, Element element) {
        TreePath elemPath = env.docs().getPath(element);
        if (elemPath == null) return Optional.empty();
        DocCommentTree tree = env.docs().getDocCommentTree(elemPath);
        if (tree == null) return Optional.empty();
        DocTreePath basePath = DocTreePath.getPath(elemPath, tree, tree);
        for (DocTree block : tree.getBlockTags()) {
            if (block.getKind() == DocTree.Kind.PARAM && block instanceof ParamTree pt) {
                if (!pt.isTypeParameter() && name.equals(pt.getName().getName().toString())) {
                    return Optional.of(HtmlConverter.asDocHtml(env, DocTreePath.getPath(basePath, pt), pt.getDescription()));
                }
            }
        }
        return Optional.empty();
    }
}
