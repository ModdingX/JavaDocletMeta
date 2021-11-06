package io.github.noeppi_noeppi.tools.java_doclet_meta.record;

import com.google.gson.JsonObject;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;
import io.github.noeppi_noeppi.tools.java_doclet_meta.record.block.DocBlockData;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.HtmlConverter;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.JsonUtil;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Optional;

public record DocData(
        String summary,
        String text,
        List<DocBlockData> properties
) {

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("summary", summary);
        json.addProperty("text", text);
        json.add("properties", JsonUtil.array(properties, DocBlockData::json));
        return json;
    }
    
    public static Optional<DocData> from(DocEnv env, Element element) {
        TreePath elemPath = env.docs().getPath(element);
        if (elemPath == null) return Optional.empty();
        DocCommentTree tree = env.docs().getDocCommentTree(elemPath);
        if (tree == null) return Optional.empty();
        DocTreePath basePath = DocTreePath.getPath(elemPath, tree, tree);
        String summary = HtmlConverter.asDocHtml(env, basePath, tree.getFirstSentence());
        String text = HtmlConverter.asDocHtml(env, basePath, tree.getFullBody());
        List<DocBlockData> properties = tree.getBlockTags().stream()
                .flatMap(tag -> DocBlockData.from(env, DocTreePath.getPath(basePath, tag), tag).stream())
                .toList();
        return Optional.of(new DocData(summary, text, properties));
    }
}
