package org.baldurs.forge.chat;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;

@ApplicationScoped
public class MarkdownToHtmlRenderService {
    Parser parser;
    HtmlRenderer renderer;

    @Inject
    ChatFrameService chatService;

    @PostConstruct
    public void init() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    @Startup
    public void start() {
        chatService.setRender(this::markdownToHtml);
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
