package org.moddingx.java_doclet_meta.record.block;

import com.google.gson.JsonObject;
import com.sun.source.doctree.*;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreeScanner;
import org.moddingx.java_doclet_meta.DocEnv;
import org.moddingx.java_doclet_meta.util.HtmlConverter;

import java.util.*;
import java.util.stream.Collectors;

public sealed interface DocBlockData permits TextBlock, ClassTextBlock {
    
    Type type();
    
    default JsonObject json() {
        JsonObject json = new JsonObject();
        addProperties(json);
        json.addProperty("type", type().name().toLowerCase(Locale.ROOT));
        return json;
    }
    
    void addProperties(JsonObject json);
    
    static List<DocBlockData> fromInline(DocEnv env, DocTreePath basePath, List<DocBlockData> blocks, List<? extends DocTree> inline) {
        // Inline return tags in the main description should act as separate block tags.
        Set<DocBlockData.Type> knownTypes = new HashSet<>(blocks.stream().map(DocBlockData::type).collect(Collectors.toUnmodifiableSet()));
        List<DocBlockData> inlineBlocks = new ArrayList<>();

        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<>() {
            @Override
            public Void visitReturn(ReturnTree tree, Void unused) {
                if (tree.isInline() && knownTypes.add(Type.RETURN)) {
                    inlineBlocks.add(new TextBlock(Type.RETURN, HtmlConverter.asDocHtml(env, DocTreePath.getPath(basePath, tree), tree.getDescription())));
                }
                return super.visitReturn(tree, unused);
            }
        };

        scanner.scan(inline, null);
        return List.copyOf(inlineBlocks);
    }
    
    static Optional<DocBlockData> from(DocEnv env, DocTreePath path, DocTree tree) {
        // Ignore parameters, they are merged with ParamData
        return Optional.ofNullable(switch (tree.getKind()) {
            case AUTHOR -> new TextBlock(Type.AUTHOR, HtmlConverter.asDocHtml(env, path, ((AuthorTree) tree).getName()));
            case DEPRECATED -> new TextBlock(Type.DEPRECATED, HtmlConverter.asDocHtml(env, path, ((DeprecatedTree) tree).getBody()));
            case EXCEPTION -> {
                ThrowsTree ex = (ThrowsTree) tree;
                yield ClassTextBlock.from(env, Type.EXCEPTION, path, ex.getExceptionName(), HtmlConverter.asDocHtml(env, path, ex.getDescription()));
            }
            case THROWS -> {
                ThrowsTree ex = (ThrowsTree) tree;
                yield ClassTextBlock.from(env, Type.THROWS, path, ex.getExceptionName(), HtmlConverter.asDocHtml(env, path, ex.getDescription()));
            }
            case PROVIDES -> {
                ProvidesTree provides = (ProvidesTree) tree;
                yield ClassTextBlock.from(env, Type.PROVIDES, path, provides.getServiceType(), HtmlConverter.asDocHtml(env, path, provides.getDescription()));

            }
            case USES -> {
                UsesTree provides = (UsesTree) tree;
                yield ClassTextBlock.from(env, Type.USES, path, provides.getServiceType(), HtmlConverter.asDocHtml(env, path, provides.getDescription()));

            }
            case RETURN -> new TextBlock(Type.RETURN, HtmlConverter.asDocHtml(env, path, ((ReturnTree) tree).getDescription()));
            case SERIAL -> new TextBlock(Type.SERIAL, HtmlConverter.asDocHtml(env, path, ((SerialTree) tree).getDescription()));
            case SINCE -> new TextBlock(Type.SINCE, HtmlConverter.asDocHtml(env, path, ((SinceTree) tree).getBody()));
            case UNKNOWN_BLOCK_TAG -> new TextBlock(Type.UNKNOWN, HtmlConverter.asDocHtml(env, path, ((UnknownBlockTagTree) tree).getContent()));
            default -> null;
        });
    }
    
    enum Type {
        AUTHOR,
        DEPRECATED,
        EXCEPTION,
        THROWS,
        PROVIDES,
        USES,
        RETURN,
        SERIAL,
        SINCE,
        UNKNOWN
    }
}
