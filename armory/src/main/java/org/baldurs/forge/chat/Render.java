package org.baldurs.forge.chat;

/**
 * Implement this and set ChatFrameService.render to your implementation if you want to customize the rendering of the chat output.
 */
public interface Render {
    String render(String chatOutput);
}
