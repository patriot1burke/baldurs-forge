package io.quarkiverse.langchain4j.chat.frames.samples;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.quarkiverse.langchain4j.chat.frames.StringMessage;
import io.quarkus.logging.Log;

public class Markdown extends StringMessage {
    static Parser parser;
    static HtmlRenderer renderer;

    static {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public static String markdownToHtml(String markdown) {
        try {
            Node document = parser.parse(markdown);
            return "<p>" + renderer.render(document) + "</p>";
        } catch (Exception e) {
            Log.error("Error rendering markdown", e);
            throw new RuntimeException(e);
        }
    }

    protected Markdown(String string) {
        super(markdownToHtml(string));
    }

    public static Markdown from(String string) {
        Log.info("MarkdownStringMessage from: " + string);
        return new Markdown(string);
    }
}
