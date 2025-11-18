package io.quarkiverse.langchain4j.chat.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.langchain4j.chat.frames")
public interface ChatFramesBuildTimeConfig {

    /**
     * Root path for chat frame endpoints.
     * By default, this value will be resolved as a path relative to `${quarkus.http.non-application-root-path}`.
     */
    @WithDefault("chat-frames")
    String rootPath();

}
