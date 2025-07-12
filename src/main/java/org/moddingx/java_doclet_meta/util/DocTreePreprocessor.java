package org.moddingx.java_doclet_meta.util;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.RawTextTree;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocTreePreprocessor {

    // See jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter$MarkdownHandler
    private static final Parser PARSER = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().omitSingleParagraphP(true).extensions(List.of(TablesExtension.create())).build();
    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("<!--\uFFFC(\\d+)-->");
    
    public static List<ProcessedDocTree> process(Element context, List<? extends DocTree> textElems) {
        if (textElems.stream().noneMatch(tree -> tree.getKind() == DocTree.Kind.MARKDOWN)) {
            return textElems.stream().<ProcessedDocTree>map(WrappedDocTree::new).toList();
        }
        StringBuilder markdown = new StringBuilder();
        List<ProcessedDocTree> replacements = new ArrayList<>();
        for (DocTree tree : textElems) {
            if (tree.getKind() == DocTree.Kind.MARKDOWN) {
                String markdownContent = ((RawTextTree) tree).getContent();
                Matcher m = REPLACEMENT_PATTERN.matcher(markdownContent);
                int start = 0;
                while (m.find()) {
                    markdown.append(markdownContent, start, m.start());
                    int replacementIdx = replacements.size();
                    replacements.add(new RawHtml(m.group()));
                    markdown.append("<!--\uFFFC").append(replacementIdx).append("-->");
                    start = m.end();
                }
                markdown.append(markdownContent.substring(start));
            } else {
                int replacementIdx = replacements.size();
                replacements.add(new WrappedDocTree(tree));
                markdown.append("<!--\uFFFC").append(replacementIdx).append("-->");
            }
        }

        Node node = PARSER.parse(markdown.toString());
        adjustHeadings(context, node);
        String htmlText = minifyHtml(RENDERER.render(node));
        return replaceElements(htmlText, Collections.unmodifiableList(replacements));
    }
    
    private static void adjustHeadings(Element context, Node markdown) {
        // See jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter$MarkdownHandler$HeadingNodeRenderer
        ElementKind kind = context.getKind();
        int headingInset = kind.isField() || kind.isExecutable() ? 3 : kind != ElementKind.OTHER ? 1 : 0;

        markdown.accept(new AbstractVisitor() {

            @Override
            public void visit(Heading heading) {
                heading.setLevel(Math.min(heading.getLevel() + headingInset, 6));
                super.visit(heading);
            }
        });
    }
    
    private static String minifyHtml(String html) {
        Document document = Jsoup.parseBodyFragment(html);
        document.outputSettings(new Document.OutputSettings()
                        .syntax(Document.OutputSettings.Syntax.html)
                        .escapeMode(Entities.EscapeMode.base)
                        .charset(StandardCharsets.UTF_8)
                        .prettyPrint(true)
                        .indentAmount(0)
                        .maxPaddingWidth(-1)
                        .outline(false)
        );
        return document.body().html().strip();
    }

    private static List<ProcessedDocTree> replaceElements(String htmlText, List<ProcessedDocTree> replacements) {
        List<ProcessedDocTree> replacedText = new ArrayList<>(2 * replacements.size() + 1);
        Matcher m = REPLACEMENT_PATTERN.matcher(htmlText);
        int start = 0;
        while (m.find()) {
            replacedText.add(new RawHtml(htmlText.substring(start, m.start())));
            int replacementIdx = -1;
            try {
                replacementIdx = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                //
            }
            if (replacementIdx >= 0 && replacementIdx < replacements.size()) {
                replacedText.add(replacements.get(replacementIdx));
            }
            start = m.end();
        }
        replacedText.add(new RawHtml(htmlText.substring(start)));
        
        List<ProcessedDocTree> result = new ArrayList<>(replacedText.size());
        for (ProcessedDocTree tree : replacedText) {
            if (tree instanceof RawHtml(String html) && html.isEmpty()) continue;
            if (tree instanceof RawHtml(String html2) && !result.isEmpty() && result.getLast() instanceof RawHtml(String html1)) {
                result.set(result.size() - 1, new RawHtml(html1 + html2));
            } else {
                result.add(tree);
            }
        }
        return List.copyOf(result);
    }
    
    public sealed interface ProcessedDocTree permits RawHtml, WrappedDocTree {}
    public record RawHtml(String html) implements ProcessedDocTree {}
    public record WrappedDocTree(DocTree tree) implements ProcessedDocTree {}
}
