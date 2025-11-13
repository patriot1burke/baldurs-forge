package io.quarkiverse.langchain4j.chat.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.DefaultChatFrame;
import io.quarkiverse.langchain4j.chat.frames.internal.ChatFrameContextImpl;
import io.quarkiverse.langchain4j.chat.frames.internal.ChatFrameControllerService;
import io.quarkiverse.langchain4j.chat.frames.internal.ChatFrameEndpoint;
import io.quarkiverse.langchain4j.chat.frames.internal.ChatFrameRecorder;
import io.quarkiverse.langchain4j.chat.frames.internal.ClientMemoryStoreBean;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.logging.Log;

public class ChatProcessor {
    public static final DotName CHAT_FRAME = DotName.createSimple(ChatFrame.class.getName());

    @BuildStep
    public void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        AdditionalBeanBuildItem.builder()
                .addBeanClasses(ChatFrameControllerService.class, ChatFrameEndpoint.class, ClientMemoryStoreBean.class,
                        ChatFrameContextImpl.class)
                .setUnremovable().build();

    }

    @BuildStep
    public void collectChatFrames(BuildProducer<ChatFrameBuildItem> chatFrameProducer,
            CombinedIndexBuildItem combinedIndexBuildItem,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        IndexView index = combinedIndexBuildItem.getIndex();
        Collection<AnnotationInstance> funqs = index.getAnnotations(CHAT_FRAME);
        Set<String> classNames = new HashSet<>();
        boolean defaultFrameFound = false;
        for (AnnotationInstance funqMethod : funqs) {
            MethodInfo method = funqMethod.target().asMethod();
            ClassInfo declaringClass = method.declaringClass();
            String className = declaringClass.name().toString();
            String methodName = method.name();
            if (Modifier.isAbstract(method.flags())) {
                throw new RuntimeException(
                        String.format("Method '%s' annotated with '@ChatFrame' declared in the class '%s' is abstract.",
                                methodName, className));
            }

            if (Modifier.isAbstract(declaringClass.flags()) || Modifier.isInterface(declaringClass.flags())) {
                throw new RuntimeException(
                        String.format(
                                "@ChatFrame is not allowed within abstract classes or interfaces. Method '%s' annotated with '@ChatFrame' is declared within the class '%s'.",
                                methodName, className));
            }

            if (!Modifier.isPublic(method.flags())) {
                throw new RuntimeException(
                        String.format("Method '%s' annotated with '@ChatFrame' declared in the class '%s' is not public.",
                                methodName, className));
            }

            if (method.returnType().kind() != Type.Kind.VOID) {
                throw new RuntimeException(
                        String.format("Method '%s' annotated with '@ChatFrame' declared in the class '%s' must return void.",
                                methodName, className));
            }

            if (method.parametersCount() != 0) {
                throw new RuntimeException(
                        String.format(
                                "Method '%s' annotated with '@ChatFrame' declared in the class '%s' must not have any parameters.",
                                methodName, className));
            }
            classNames.add(className);

            String frameName = className + "::" + methodName;
            if (funqMethod.value() != null) {
                frameName = funqMethod.value().asString();
            }

            boolean defaultFrame = method.hasAnnotation(DotName.createSimple(DefaultChatFrame.class));
            if (defaultFrame) {
                if (defaultFrameFound) {
                    throw new RuntimeException(
                            String.format(
                                    "Multiple default chat frames found. Only one @DefaultChatFrame chat frame is allowed per deployment.",
                                    methodName, className));
                }
                defaultFrameFound = true;
            }
            chatFrameProducer.produce(new ChatFrameBuildItem(frameName, className, methodName, defaultFrame));
        }
        if (!classNames.isEmpty()) {
            for (String className : classNames) {
                reflectiveClass.produce(ReflectiveClassBuildItem.builder(className).methods().build());
            }
            additionalBeanProducer.produce(AdditionalBeanBuildItem.builder().addBeanClasses(classNames)
                    .setDefaultScope(DotNames.APPLICATION_SCOPED).setUnremovable().build());
        }
    }

    @BuildStep
    @Record(STATIC_INIT)
    public void registerChatFrames(ChatFrameRecorder recorder, RecorderContext context,
            List<ChatFrameBuildItem> chatFrameBuildItems) {
        if (chatFrameBuildItems.size() == 1) {
            Log.info("There is only one chat frame so setting default chat frame to "
                    + chatFrameBuildItems.get(0).getFrameName());
            ChatFrameBuildItem chatFrame = chatFrameBuildItems.get(0);
            recorder.registerChatFrame(chatFrame.getFrameName(), context.classProxy(chatFrame.getClassName()),
                    chatFrame.getMethodName(), true);
        } else {
            boolean hasDefaultFrame = false;
            for (ChatFrameBuildItem chatFrame : chatFrameBuildItems) {
                Log.info("Registering chat frame: " + chatFrame.getFrameName());
                if (chatFrame.isDefaultFrame()) {
                    Log.info("Default chat frame: " + chatFrame.getFrameName());
                }
                recorder.registerChatFrame(chatFrame.getFrameName(), context.classProxy(chatFrame.getClassName()),
                        chatFrame.getMethodName(), chatFrame.isDefaultFrame());
                if (chatFrame.isDefaultFrame()) {
                    hasDefaultFrame = true;
                }
            }
            if (!hasDefaultFrame) {
                Log.warn(
                        "No default chat frame found.  Use @DefaultChatFrame on at least one @ChatFrame method, or you will have to pass chat frame with context");
            }
        }
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public void setContainer(ChatFrameRecorder recorder, BeanContainerBuildItem beanContainerBuildItem) {
        recorder.setContainer(beanContainerBuildItem.getValue());
    }

}
