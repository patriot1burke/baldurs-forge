# Quarkus Chat Frames

Quarkus Chat Frames is an extension built on top of [Quarkus Langchain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) to help build chat applications.

* Provide a remote invocation framework for AIServices for clients (mobile and web UIs, REST or Websocket)
* Provide a simple event based API between clients and server
* Provide simpler chat memory management for chat applications that manage interactions with multiple prompts
* Provide a way to define simple flow in a chat application that has multiple prompts
* Provide session capabilities for REST based applications
* Provide chat conversation scoped data
* Provide a way to map AIService results to structures consumable by client (i.e. markdown to html, json to html, etc.)


## Learn by example

Let's learn Quarkus Chat Frames by slowly iterating and building up a full featured chat application.  There will be a 
lot of pseudo code and hand waving in this example, but hopefully you'll get the gist of things.

## The Example App

The example app is a web application that has chat interface.  It's purpose is to design mod addons for
the 2023 Game of the Year, Baldur's Gate 3 (BG3), a Dungeons & Dragons RPG video game.  This is an actual application that I wrote and it is called Baldur's Forge.  It's still a work in
progress, but its fairly functional.  The app currently let's you do natural language queries on the game's magical armor and weapons to find out information about them and to see what their stats are.  It also let's you build new armor and weapons, package it as a mod, and use these new items in game.

Let's go through the evolution of this application to illustrate various concepts of Quarkus Chat Frames.

## Remote Inovcation for an AI Service

When I started Baldur's Forge, I knew very little about [Quarkus Langchain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) and AI in general, but I wanted to create a pure natural language chat interface
for building a BG3 mod.  The first bit of functionality I wrote was the ability to write a natural langauge query for magical items that came with the BG3 game.  Yup!
I needed RAG. I used [Quarkus Langchain4j Easy RAG](https://docs.quarkiverse.io/quarkus-langchain4j/dev/rag-easy-rag.html) to get
a RAG prompt up and running.  It was insanely easy.  

The first thing I had to do was create a text file that described each and every item in my BG3 equipment database.  I stored this in a directory and all I had to do
was point Quarkus to it:

```conf
quarkus.langchain4j.easy-rag.path=/home/bburke/bg3/db/textfiles
```

The next thing to do was to create an AiService, the definition of the prompt that would be used with the LLM:

```java
@RegisterAiService
public interface RagPrompt {

    @SystemMessage("""
            You are a database for magical armor and weapons in a Dungeons and Dragon's game.  Answer questions about these items.
            Your response must be polite, use the same language as the question, and be relevant to the question.

            When you don't know, respond that you don't know the answer.
            """)
    String chat(@UserMessage String question);
}
```

That was it!!

Calling `RagPrompt.chat()` makes a RAG query internally and feeds the results and your question to the LLM and returns a string response.  Quarkus
Easy RAG wired up everything automatically for me.  All this is basic Quarkus Langchain4j.  Let's look at what Chat Frames could help me with!

Now, after I had a working RAG prompt engine, I wanted to wire up my JQuery-based Web client.  I needed a way to invoke on the AiService from my web chat client.  
This is where Chat Frames helps out as it can provide a remote interface for your AI Services.  Here's the server code:

```java
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;

@RegisterAiService
public interface RagPrompt {

    @SystemMessage("""
            You are a database for magical armor and weapons in a Dungeons and Dragon's game.  Answer questions about these items.
            Your response must be polite, use the same language as the question, and be relevant to the question.

            When you don't know, respond that you don't know the answer.
            """)
    @ChatFrame("dnd-db")
    String chat(@UserMessage String question);
}
```

Just annotating `RagPrompt.chat()` with `@ChatFrame` makes it remotely invokable.  Quarkus Chat Frames automatically starts an HTTP endpoint that can receive
JSON messages sent with an HTTP POST (Rest), or via a Web Socket Channel.  By default he URL path for this is `/q/chat-frames` + the name of the chat frame.  So, in this case
the path is `/q/chat-frames/dnd-db`.  

For clients, the initial JSON Message to the server is simple:

```json
{
    "userMessage": "What are some weapons that are effective against giants?"
}
```

Here's how the JavaScript client looks:

```javascript
    let chatContext = null;  // more on this variable later!!

    async function sendChat(message) {
        try {
            let postData = null;
            let request = {
                userMessage: message,
                context: chatContext
            }
            postData = JSON.stringify(request);

            const response = await $.ajax({
            url: `/q/chat-frames/dnd-db`,
                        type: 'POST',
                        data: postData,
                        processData: false,
                        contentType: false
            });
```

The JSON message sent by the JavaScript is sent to the server and the `RagPrompt.chat()` method is invoked based on that JSON payload.
The AiService executes and a response JSON document is sent back to the chat frame client with the following format:

```json
{
    "events": [
                {"type": "eventType" , "value" : ...anything...}..
              ],
    "context": {...}
}
```

The *"events"* field contains a list of event objects.  *"type"* is the kind of event.  The *"value"* can be anything (primitive, string, or another json object).
The *"context"* field is only sent back with Rest invocations.  This is an opaque field that contains chat conversation state and should be resent back to the server on future chat requests.

For our chat example, Quarkus Chat Frames automatically converts the String response from the `RagPrompt.chat()` method to a *StringMessage* event type:

```json
{
    "events": [ 
        {"type": "StringMessage", "value": "There are a number of weaponst that can help fight dragons..."}
    ],
    "context": {...}
}
```

The finished Javascript client looks like this:

```javascript
    let chatContext = null;  // more on this variable later!!

    async function sendChat(message) {
        try {
            let postData = null;
            let request = {
                userMessage: message,
                context: chatContext
            }
            postData = JSON.stringify(request);

            const response = await $.ajax({
            url: `/q/chat-frames/dnd-db`,
                        type: 'POST',
                        data: postData,
                        processData: false,
                        contentType: false
            });
            // Don't forget to save the context for use in the next conversational chat call!
            chatContext = response.context;

            if (response.events) {
                for (const event of response.events) {
                    if (event.type === 'StringMessage') {
                        handleStringMessage(event.value);
                    }
                }
            }
```

The client just loops through the events returned by the server and calls a handle method based on the type.  We'll see later how you can send your own
application specific event types back to the client.

Notice also that the global variable `chatContext` is set with the response's *"context"* field.  This context data must be passed with the next chat request.  It keeps conversational state for Rest clients.

*TODO: Give a Web Socket example*

## Securing Chat Frames

The endpoint for chat frames is a non-application-based URL path.  You can secure chat frame endpoints by using built in Quarkus http security features and annotations.  Here's an example
of setting up basic auth and adding role-based security.  First set up `application.properties`:

```conf
quarkus.http.auth.basic=true
quarkus.http.auth.permission.roles1.paths=/q/chat-frames
quarkus.http.auth.permission.roles1.policy=authenticated
```

This is just basic [Quarkus HTTP Security](https://quarkus.io/guides/security-basic-authentication).  In the file above, we are turning on basic authentication and saying that we want the chat frames endpoint and subpaths to require
authentication.  How you set up users and role mappings depends on how you set up Quarkus security.  We won't go into that as its just basic Quarkus.

Security annotations like `@RolesAllowed` can be used on `@ChatFrame` methods:

```java
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import jakarta.annotation.security.RolesAllowed;

@RegisterAiService
public interface RagPrompt {

    @SystemMessage("""
            You are a database for magical armor and weapons in a Dungeons and Dragon's game.  Answer questions about these items.
            Your response must be polite, use the same language as the question, and be relevant to the question.

            When you don't know, respond that you don't know the answer.
            """)
    @ChatFrame("dnd-db")
    @RolesAllowed("user")
    String chat(@UserMessage String question);
}
```

## Decoupling Chat Frames from AI Services

Sometimes we may not want our UI client interacting directly with an Ai Service.  You do not have to use `@ChatFrame` with an Ai Service.
You can write bean classes and annotation methods with `@ChatFrame` so that these methods can be invoked in the same way as previous examples.


```java
@ApplicationScoped
public class PromptProcessor {
    @Inject
    RagPrompt prompt;

    @ChatFrame("dnd-db")
    String wrapIt(@UserMessage msg) {
        // Do some preprocessing
        String chatMsg = prompt.chat(msg);
        // Do some post processing 
        chatMsg = markdown2html(chatMsg);
        return chatMsg;
    }
}
```

## Sending back multiple events via ChatFrameContext

So, in the RAG example above for my BG3 application, the UI was a little boring and not that helpful.  Sure it could
answer questions about BG3 armor and weapons, but it would only give summary answers.  I wanted it to actually render something
nice for each armor or weapon it found, then give a summary.  I had access game icons and a structured database for each entry.
It made sense for the LLM to find these items and give a summary and give answers to my questions, but it didn't make sense for the LLM
to render the items found.  Rendering would just be classic client-server-CRUD.  But how to mix the two together?

The first thing I did had nothing to do with chat frames.  I had to ditch Easy RAG and do
embedding queries directly. I had to associate metadata with each embedding so that I could pull the ID of the items the LLM found so I could
get a data structure from the DB representing those items.  Once I had a list of objects, I could send those items back to the UI client to be rendered
along with the answer to the question the LLM provided.  The AiService prompt had to change a little but, but I won't get into that.  Here's some
pseudo code to illustrate what I'm talking about:

```java
@ApplicationScoped
public class RagAgent {
    @Inject
    RagPrompt prompt;

    @Inject
    GameEquipmentDB db;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    @ChatFrame("dnd-db")
    public String query(@UserMessage userMsg, ChatFrameContext ctx) {
 

        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(
            request(userMsg)
        );

        List<Equipment> relatedItems = search.matches().stream().map(m -> {
            String id = m.embedded().metadata().getString("id");
            return db.get(id);
        }).toList();

        ctx.addEvent("EquipmentList", relatedItems); 

        String llmSummary = prompt.chat(userMsg, relatedItems);
        return llmSummary;
    }

    private EmbeddingSearchRequest request(String userMessage) {
        Embedding embedding = embeddingModel.embed(queryString).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .minScore(0.75)
                .maxResults(5)
                .build();
        return request;
    }
}
```
If you look at the `query()` method you see that it is a `@ChatFrame`.  It's parameters are the user message and a `ChatFrameContext`.
The `ChatFrameContext` is utility interface for Quarkus Chat Frames.  For this example, we'll use it to piggyback an additional event
back to the client.

Here is what our Java code is doing:


1. First, a search request is created using the user message and the embedding model.
2. Next, the search is executed on the embedding store of the BG3 equipment DB.  This returns a set of `TextSegments` with metadata associated with it.
3. The matches are iterated upon.  Each embedding has ID metadata associated with it.  This ID is used to query the BG3 database to get the DAO of the item.
4. Now that we have the list of equipment, that list is sent back as an event to the client by calling `ctx.addEvent()`.  The client uses this event to create a nice rendering of each item found.
5. The user message and list of related equipment is sent to the AiService prompt so that the user message query can be answered
6. We return the answer the LLM found too.  (This creates an additional event).

Here is the JSON sent back to the client:
```json
{
    "events": [
        {"type": "ListEquipment", "value": [...]},
        {"type": "StringMessage", "There are a number of weapons that can help fighting giants..."}
    ],
    "context": {...}
}
```

Our web client needs to change to support handling the *ListEquipment* event type.

```javascript
    async function sendChat(message) {
        try {
            let postData = null;
            let request = {
                userMessage: message,
                context: chatContext
            }
            postData = JSON.stringify(request);

            const response = await $.ajax({
            url: `/q/chat-frames/dnd-db`,
                        type: 'POST',
                        data: postData,
                        processData: false,
                        contentType: false
            });
            chatContext = response.context;
            if (response.events) {
                for (const event of response.events) {
                    if (event.type === 'StringMessage') {
                        handleStringMessage(event.value);
                    } else if (event.type === 'ListEquipment') {
                        renderEquipmentList(event.value);
                    }
                }
            }
```

Here's what it looks like in the UI:

![Screenshot](query1.png)

If you mouse over a listed item, it pops up a tooltip of the actual item, which is what is shown in the picture.  

![Screenshot2](query1_plus_tooltip.png)

All this is just custom client code using JQuery and nothing
to do with the LLM.  We have successfully mixed traditional CRUD UI techniques with the LLM.

FYI: In the final implementation I actually decided I didn't care at all about LLM answers to the user message query.  All I cared about was the list of related
items to my user request.  The LLM response was often inconsistent, irrelevant, and even weird or wrong.  All things you DO NOT WANT in a good UI! So, I removed the
last interaction with the LLM to get a summary and only used the LLM for creating an search embedding.

The final implementation of this RAG query was part of a larger application that I'll dive into later when we talk about other Chat Frame features, but the
code that invokes the rag request is [here](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/mainmenu/MainMenuToolBox.java#L70), the implementation of the request is [here](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/services/EquipmentDB.java#L256), and the UI code is [here](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/META-INF/resources/index.html#L1274).  

## Chat Memory and Default Memory Ids with Chat Frames

I've written a blog that talks about how chat memory works with Quarkus Langchain4j.  Chat Frames expands on this chat memory management.

## Nested Conversations

Want to add weapons/armor.

### Main Menu Prompt and tools

### Create Weapon needs its own conversation (pushFrame)

### Create weapon chat needs to build up a document

### Finish the weapon and go back to main menu

