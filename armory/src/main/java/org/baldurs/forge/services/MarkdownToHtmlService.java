package org.baldurs.forge.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.quarkus.logging.Log;

@ApplicationScoped
public class MarkdownToHtmlService {
    Parser parser;
    HtmlRenderer renderer;

    @PostConstruct
    public void init() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public String markdownToHtml(String markdown) {
        try {
            Node document = parser.parse(markdown);
            return "<p>" + renderer.render(document) + "</p>";
        } catch (Exception e) {
            Log.error("Error rendering markdown", e);
            return markdown;
        }
    }

}
