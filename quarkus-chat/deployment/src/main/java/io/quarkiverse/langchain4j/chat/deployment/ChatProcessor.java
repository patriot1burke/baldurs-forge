package io.quarkiverse.langchain4j.chat.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkiverse.langchain4j.chat.context.ChatContext;
import io.quarkiverse.langchain4j.chat.context.ClientMemoryStoreBean;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameEndpoint;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameService;

public class ChatProcessor {

    @BuildStep
    public void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        AdditionalBeanBuildItem.builder()
                               .addBeanClasses(ChatFrameService.class, ChatFrameEndpoint.class, ClientMemoryStoreBean.class, ChatContext.class)
                               .setUnremovable().build();

    }

}
