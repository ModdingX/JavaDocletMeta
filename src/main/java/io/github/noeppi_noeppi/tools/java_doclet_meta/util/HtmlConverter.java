package io.github.noeppi_noeppi.tools.java_doclet_meta.util;

import com.sun.source.doctree.*;
import com.sun.source.util.DocTreePath;
import io.github.noeppi_noeppi.tools.java_doclet_meta.DocEnv;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import java.util.List;

public class HtmlConverter {
    
    public static String asDocHtml(DocEnv env, @Nullable DocTreePath basePath, List<? extends DocTree> textElems) {
        StringBuilder sb = new StringBuilder();
        for (DocTree tree : textElems) {
            DocTreePath path = basePath == null ? null : DocTreePath.getPath(basePath, tree);
            switch (tree.getKind()) {
                case ENTITY -> sb.append(((EntityTree) tree).getName());
                case START_ELEMENT -> {
                    StartElementTree elem = (StartElementTree) tree; 
                    sb.append("<").append(elem.getName());
                    for (DocTree attrTree : elem.getAttributes()) {
                        if (attrTree.getKind() == DocTree.Kind.ATTRIBUTE && attrTree instanceof AttributeTree attr) {
                            sb.append(" ").append(attr.getName().toString());
                            if (attr.getValueKind() != AttributeTree.ValueKind.EMPTY) {
                                sb.append("=\"");
                                // Only supports plain text
                                for (DocTree attrContentTree : attr.getValue()) {
                                    if (attrContentTree.getKind() == DocTree.Kind.TEXT) {
                                        sb.append(((TextTree) attrContentTree).getBody()
                                                .replace("\\", "\\\\")
                                                .replace("\"", "\\\"")
                                        );
                                    }
                                }
                                sb.append("\"");
                            }
                        }
                    }
                    sb.append(">");
                }
                case END_ELEMENT -> {
                    EndElementTree elem = (EndElementTree) tree;
                    sb.append("</").append(elem.getName()).append(">");
                }
                case CODE -> sb.append("<code>").append(HtmlQuote.quote(((LiteralTree) tree).getBody().getBody())).append("</code>");
                case LITERAL -> sb.append("<literal>").append(HtmlQuote.quote(((LiteralTree) tree).getBody().getBody())).append("</literal>");
                case ERRONEOUS -> sb.append(HtmlQuote.quote(((ErroneousTree) tree).getBody()));
                case LINK -> sb.append(linkTag(env, "ref", path, ((LinkTree) tree).getReference(), ((LinkTree) tree).getLabel()));
                case LINK_PLAIN -> sb.append(linkTag(env, "refp", path, ((LinkTree) tree).getReference(), ((LinkTree) tree).getLabel()));
                case VALUE -> sb.append(inlineValue(env, path, ((ValueTree) tree).getReference()));
                case SYSTEM_PROPERTY -> sb.append("<system_property>").append(HtmlQuote.quote(((SystemPropertyTree) tree).getPropertyName().toString())).append("</system_property>");
                case IDENTIFIER -> sb.append(HtmlQuote.quote(((IdentifierTree) tree).getName().toString()));
                case TEXT -> sb.append(HtmlQuote.quote(((TextTree) tree).getBody()));
                case UNKNOWN_INLINE_TAG -> {
                    UnknownInlineTagTree tag = (UnknownInlineTagTree) tree;
                    sb.append("<").append(tag.getTagName()).append(">");
                    sb.append(asDocHtml(env, path, tag.getContent()));
                    sb.append("</").append(tag.getTagName()).append(">");
                }
            }
        }
        return sb.toString();
    }
    
    private static String inlineValue(DocEnv env, @Nullable DocTreePath basePath, ReferenceTree reference) {
        DocTreePath path = basePath == null || basePath.getLeaf() == reference ? basePath : DocTreePath.getPath(basePath, reference);
        Element target = path == null ? null : env.docs().getElement(path);
        if (target != null && target.getKind() == ElementKind.FIELD && target instanceof VariableElement var) {
            Object constant = var.getConstantValue();
            if (constant != null) {
                return "<code>" + HtmlQuote.quote(env.elements().getConstantExpression(constant)) + "</code>";
            }
        }
        // If no field was found or no constant expression exists, just link the member
        return linkTag(env, "ref", basePath, reference, (String) null);
    }
    
    private static String linkTag(DocEnv env, String tagName, @Nullable DocTreePath basePath, ReferenceTree reference, @Nullable List<? extends DocTree> textContent) {
        if (textContent == null || textContent.isEmpty()) {
            return linkTag(env, tagName, basePath, reference, (String) null);
        } else {
            return linkTag(env, tagName, basePath, reference, asDocHtml(env, basePath, textContent));
        }
    }
    
    private static String linkTag(DocEnv env, String tagName, @Nullable DocTreePath basePath, ReferenceTree reference, @Nullable String textContent) {
        if (textContent == null) textContent = reference.getSignature();
        String ref = reference.getSignature();
        DocTreePath path = basePath == null || basePath.getLeaf() == reference ? basePath : DocTreePath.getPath(basePath, reference);
        Element target = path == null ? null : env.docs().getElement(path);
        String params = target == null ? null : getTargetParams(env, target);
        if (params == null) {
            return HtmlQuote.quote(ref);
        } else {
            return "<" + tagName + params + ">" + HtmlQuote.quote(textContent) + "</" + tagName + ">";
        }
    }

    // Returned string must be empty or start with a space
    private static String getTargetParams(DocEnv env, Element target) {
        if (target.getKind() == ElementKind.PACKAGE && target instanceof PackageElement pkg) {
            return " pkg=\"" + pkg.getQualifiedName().toString().replace('.', '/') + "\"";
        } else if ((target.getKind().isClass() || target.getKind().isInterface())) {
            return getClassTargetParams(env, target);
        } else if (target.getKind().isField() && target instanceof VariableElement field) {
            return getClassTargetParams(env, target.getEnclosingElement()) + " field=\"" + field.getSimpleName().toString() + "\"";
        } else if (target.getKind() == ElementKind.METHOD && target instanceof ExecutableElement method) {
            return getClassTargetParams(env, target.getEnclosingElement()) + " method=\"" + method.getSimpleName().toString() + "\" type=\"" + method.asType() + "\"";
        } else if (target.getKind() == ElementKind.RECORD_COMPONENT && target instanceof RecordComponentElement component) {
            return getClassTargetParams(env, target.getEnclosingElement()) + " component=\"" + component.getSimpleName() + "\"";
        } else {
            return null;
        }
    }
    
    private static String getClassTargetParams(DocEnv env, Element target) {
        if ((target.getKind().isClass() || target.getKind().isInterface()) && target instanceof TypeElement type) {
            return " cls=\"" + env.elements().getBinaryName(type).toString().replace('.', '/') + "\"";
        } else {
            return "";
        }
    }
}
