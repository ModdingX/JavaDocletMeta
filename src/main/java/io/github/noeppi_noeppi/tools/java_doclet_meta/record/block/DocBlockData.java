package io.github.noeppi_noeppi.tools.java_doclet_meta.record.block;

import com.google.gson.JsonObject;
import com.sun.source.doctree.*;
import com.sun.source.util.DocTreePath;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;
import io.github.noeppi_noeppi.tools.java_doclet_meta.util.HtmlConverter;

import java.util.Locale;
import java.util.Optional;

public interface DocBlockData{
    
    Type type();
    
    default JsonObject json() {
        JsonObject json = new JsonObject();
        addProperties(json);
        json.addProperty("type", type().name().toLowerCase(Locale.ROOT));
        return json;
    }
    
    void addProperties(JsonObject json);
    
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
