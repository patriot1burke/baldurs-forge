# Baldur's Forge
Project that provides tools and services for Baldur's Gate 3

## Baldur's Armory

AI/LLM Chatbot for Baldur's Gate 3.  Search the item DB extracted from Baldur's Gate 3.  Import items from other mods to the DB.  All through an AI chat interface.  
Create new equipment mods from this chatbot too.

## Baldur's Archivist

Java utility library for dealing with various BG3 file formats:  .pkg, .lsf, .lsx, etc.


# Why did I create this project?

* I wanted something more complex than __hello world__ that was a semi-real application.
* I wanted to create a natural language chat interface for a CRUD application.
* I wanted to relearn JQuery and CSS
* I wanted to seriously test drive Cursor AI IDE

# Features

* Entire Baldur's Gate 3 item and spell database is imported
* Chat commands to search the database.  Nice tooltips are rendered to display searches.  For example you can say: __Show me weapons that deal radiant damange__
* You can create new items and give them enchantments
* You can package new items into a mod that is usable within the game
* You can import an existing mod into the internal database

The app is functional, but not fully featured.  Would need a lot more work to get complete coverage for item creation.

# Terms you should know

* Tools - functions published to the LLM that the LLM can decide to invoke
* RAG - go look it up
* Prompt - basic term.  go look it up.  
* Chat Memory - History of user chat messages and LLM responses.  It is passed to the LLM with every chat request.

# High level architecture:

## Software Stack
* Java and [Quarkus](https://quarkus.io) for the Server
* quarkus-rest-jackson
* [quarkus-langchain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) AI library I used.
* Open AI, with 4o language model
* [PGVectorDB](https://github.com/pgvector/pgvector)
* JQuery for browser client

## 6 Different prompts

* [Main Menu Prompt](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/mainMenuCommands.txt).  List of high level commands that can delegate to other prompts and tools.
* 2 Metadata prompts for RAG.  Item [type](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/equipmentType.txt) and [Slot](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/equipmentSlot.txt)
* Natural language to [enchantment](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/nl2boost.txt) macro prompt.  BG3 has a rich macro language for application enchantments to magical items.  This prompt converts natural language to this macro language
* General prompt for [equipment building](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/equipmentBuilder.txt)
* Prompt for gathering metadata to [package the BG3 mod](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/equipmentModPackager.txt)

## Main Menu Tool Box

The [Main Menu Prompt](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/mainMenuCommands.txt) is hooked up to a list of [tool methods](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/chat/MainMenuCommands.java).  Each of these tools can perform a capability of the application.  Creating new items, finding an item, searching for an item,
editing or deleting an item, or packaging up a mod.  There is a tool for each of these actions and these tools use other prompts and services to complete the actions.

## Basic RAG with Metadata Prompts

I imported every BG3 item into a custom in-memory DB.  From that in-memory DB I generate a document describing each item in the DB.  Its type, properties, enchantments, etc.  This document
is semi-structured english descriptions.  Each document is sent to Open AI to create embeddings for searches.  These embeddings are stored in a vector DB.  Each
item stored in the vector DB has metadata associated with it:  ID, Type (Weapon or Armor), Slot (Head, Body, Glove, Boot, Necklace etc...)

For search queries, the user message is filtered through a specific prompt to identify the type (Weapon or Armor).  The same user message is sent through a another prompt to 
extract the Slot.  This metadata is used as a filter to the vector DB.  This is Basic RAG.  Look at my code or do a search on RAG to understand this basic well-known pattern.

## Find vs. Search

The Main Menu Prompt has a find command which can find an item by name via a hash lookup, and a search command that is for broader general RAG queries.  When the user asks to find something, the LLM matches to the findBy tool.  If the item can be found by name, it returns it.  Otherwise it throws an exception whose message states the item is not found and that a more general search should be done.
The LLM understands this and immediately calls search which does a RAG query.  It was pretty cool how this just worked (until lately, when it just decided not to work!).  That the combination of langchain4j and OpenAI could catch the exception, process it, and invoke a different appropriate tool.

## Chat Context

I found that tools need to store structered information in between chat messages.  I call this the [Chat Context](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/context/ChatContext.java).  For example, when building a piece of armor, the current representation of the new armor
is stored in the chat context so that it can be accessed between chat messages.

The client sends the *Chat Context* as a json document to the server.  It contains the user message and a map of context data returned from the server.
The server sends the *Chat Context* as a response to any chats.  It can contain one or more response objects, the client side chat memory (see later), and context data
that tools might need to perform future actions.

## Chat Frames

Some prompts, like the natural language to enchantment macro prompt, are just input and output.  There is no conversation or chat memory needed.

Other prompts need to have a conversation with the user.  The web client does not make the decision on what prompt to call.  It just sends posted text to the server.
The server knows what the current prompt is and calls it.  Tools can decide to change the current chat prompt.  I call this *Chat Frames*.  The *Chat Context*
is used to set the current prompt between chat messages.

When a chat frame is pushed or poped, the current chat memory is cleared.

## [Client-side Chat Memory](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/context/ClientMemoryStore.java)

I needed per-user chat memory.  I didn't like the idea of storing chat memory on the server as I prefer my services to be stateless.  Obviously, being stateless solves
a lot of architectural issues as there's no need to have sticky sessions or distributed cache to hold session state.  So, I developed client side chat memory which simply
serializes chat memory into json and stores it in the *Chat Context*.  Client memory is piggy-backed with the client user message when chatting with the server.

I had to do a few hacks to make this work with Quarkus correctly.  All that is documented in the code.  Take a look!

## Tool/UI Messages

As you'll see in the *Lessons Learned* section, it was quite difficult to get the LLM to return formatted output.  So, what I implemented was that tools can piggyback
additional response messages to the client.  These messages tell the client to perform specific actions and could provide data to perform those actions.  For example, search
provides a list of equipment.  This is sent back to the client as a ListEquipment message.  From that message, the client renders something nice a specific.

## LLM Json Document Builder

For general data gathering (equipment building and mod packaging) I used prompt/tool pattern of building a JSON document.  The prompt would provide
a JSON schema and the current JSON built document and ask the user to fill out the fields that haven't been set yet.  I used 2 different tool patterns.

If you look at [ModPackager](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/builder/ModPackager.java) you'll see that the LLM will call the [updatePackage](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/builder/ModPackager.java#L98) tool method passing in json of the package representation.
The way Open AI 4o works(sometimes!) is that it will pass in the PackageModel with only the fields set in the user message.  The `updatePackage`tool method figures out what fields were set
and updates the current json object and stores it within the *Chat Context*.  When the user says they are finished, the [finishPackage](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/builder/ModPackager.java#L117) tool method is invoked by the LLM and sends back a message to the client through the *Chat Context* to
package up the mod.

Building items (magic rings, armor, weapons, etc..) is a little different.  Instead of having an `update` method, I have a tool method that sets each and every property of the json document.  An example is the [setName](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/builder/WeaponBuilder.java#L52).  Why do I do it this way?  Well...I used to use
the `update` tool method pattern I used for `ModPackager`, but one day, after like 2 months, Open AI 4o decided not to call the `update` method consistently.

# Lessons Learned

The most frustrating thing about developing an AI/LLM chatbot was how wildly inconsistent it was.  Stuff that would work one day (or one week, or one month even!)
would not work the next, then start working again, then stop again.  This caused me to constantly refine how I was working with it.  
This was most of the work: getting consistent and deterministic results from the AI.  It could be the model I was using (OpenAI 4o).

Here's more specific lessons learned:

## Keep prompts focused

This is the one consistent thing you could find on the web on how to write prompts.  Keep them focused on a specific thing you want to do.  If you try to write
one prompt to rule them all the LLM will get confused and may or may not invoke the tools or spit out the text you want.

## Provide examples with sample input and output.  Or sample input and what action you want the AI to take

Initiailly, the main menu prompt was very simple.  I relied on the LLM to match the user message to the list of tools that were provided.
The LLM ended up being really inconsistent on whether or not it would call the appropriate tool or not.
What I finally did was rewrite the main menu promopt with a set of examples of input user messages and actions to take based on that input.  Without
those example, the LLM was really inconsistent.  Sometimes it would call a tool, sometimes it wouldn't.  Depended on its mood!

## You cannot trust the AI to provide consistently formatted output.

I honestly gave up on trying to get the AI to output HTML.  I even tried returning HTML output from tool methods
and the LLM would just ignore it, interpret the response from the tool and output anything it wanted.  No matter what I put in the
prompt, OpenAI would almost always return Markdown.  I was able to get it to output strict json schema, but sometimes it would return
 json embedded within Markdown.  It was very frustrating.

 This is another reason I created *Chat Context*.  When a tool needs something rendered, I piggyback a structured Message object on the chat response.
 You can find examples of this all over the place in my code.

## Need to communicate session state between tools and client

For the, browser clients and server-side prompts and tool methods to even be able to work, I needed a way to pass session
information between them.  Chat memory just wasn't enough.  In fact, chat memory often caused problems (see other issues).

## Your client code should format complex visual responses itself

If you want a nice interface with complex output that is formatted really nicely, you _WILL NOT_
be able to use the LLM output as-is.  You'll need to extract the appropriate data structure from LLM responses and send it back to the client
to be rendered correctly using human written code.

## AI responses will be inconsistent.  You cannot guarantee what the AI will output

I originally had my search return multiple items in a json array and I'd ask the AI to list and summarize the search.
Sometimes it would provide a numbered list.  Sometimes bulleted.  Sometimes it would produce a paragraph with the names
of things that it found. Thus, I couldn't create a consistent and nice looking UI.  I gave up trying to get the AI to produce
nice output.

## void tool responses confuse the AI

For instance, I had a tool method `void listEquipment()`  It would invoke the tool correctly, but then output
that it could not find anything to list even though I would tell the prompt and tell description not to output anything
when `listEquipment` was called.

## Tools will need access to the original user message

Multiple tool implementations in my code needed to get access to the
current user message to get more accurate results from the LLM.  "Why, you ask?"  Read on dear reader!

## You can't guarantee what the LLM will send to tool parameters

For example, I had a search tool method that took a string query parameter.  The AI would look at the user message and extract
keywords before calling the search method.  This would screw up search results.  I had to manually make the raw user message
available to the tool function.  Context matters and keywords can lose context.

Take a look at the [addBoost](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/java/org/baldurs/forge/builder/EquipmentBuilder.java#L163) tool method.
The LLM would look at the following user message and invoke the `addBoost` method.

*create new longsword with boost of +3*

Only *+3* would be sent as a parameter and it would not know if it was armor or a weapon and didn't know whether to 
output AC(3) or WeaponEnchantment(3).
Again, I had to send get access to the original user message and send that to the boost prompt.

## Chat memory can confuse the AI

When invoking the [enchantment](https://github.com/patriot1burke/baldurs-forge/blob/main/armory/src/main/resources/prompts/nl2boost.txt) macro prompt, 
I originally hooked up this call to chat memory.  The 2nd time `addBoost` tool was called within a chat session
the call to the `enchantment` prompt would convert the user message , but *ALSO* would look in chat history and convert a previous add boost user message request.

For example:

```
User:  add boost advantage on saving throws
AI: Ai would return Advantage(SavingThrows) from enchantment prompt
User:  add boost +3 weapon enchantment
AI:  Ai would return Advantage(SavingThrows);WeaponEnchantment(3)
```

To solve this, I turned off chat memory for enchantment prompt invocations (aka I removed the @MemoryId parameter to the chat method call)

## You can't guarantee that the AI will call a tool

I used to have the enchantment prompt as part of the @ToolBox for `WeaponBuilderChat.buildWeapon` (and the other builders).  For a long long time
it would consistently convert a boost description to a boost macro by calling the enchantment prompt before invoking `setBoost/addBoost` tool methods.
*Then it just stopped working consistently!!!*  For no reason at all OpenAI would or would not call the enchantment prompt tool. It was different every time.

An even simpler example is that I have a `setBoost` tool and a `addBoost` tool which is called to set the enchantment macro.  `setBoost` clears the existing value
of the boost and resets it with the new.  `addBoost` tool adds an additional macro to the existing enchantment.
Simple right?  So, it sometimes works, and sometimes doesn't.  For instance, I type `add boost +3 to weapon` it sometimes calls the `addBoost` tool other times it calls
the `setBoost` tool.  Frustrating!  The workaround was to explicitly state in the prompt when to call the add or set boost tool.  At least, I hope that's the workaround!!!
I wouldn't be surprised if this just stopped working randomly someday.

## Prompts and Code will break with another LLM model

I spent most of my time with Open AI 4o.  After I got a working application, I decided to first try Ollama and llama3.  I gave up quickly
as it couldn't even process the main menu commands.  Qwen 3.2 model worked a little better.  Main menu command processing __sort of__ worked,
but LLM output had *thinking* reasoning logic in it.  You can turn this off, but the `<thinking>` XML blocks were still there which through off
all my text processing.  I quickly realized that it would take a considerable amount of time to port my app to another model.  I'm going to write 
another blog about this.

# Experiences with Cursor IDE

This project was the first time I ever used Cursor IDE.  I love Cursor and can't live without it, but when I first started
using it I almost uninstalled it immediately.  The first thing I asked Cursor chat window to do was to generate me a quarkus application
that used quarkus-langchain4j.  Oh, it generated a complete project, that compiled and everything.  Except, this application didn't do anything.
It _looked_ liked it should do something.  It created embedding interfaces and stuff that looked like it should be for quarkus-langchain4j, but
it wasn't.  A complete wild goose chase and waste of time.  I found that anytime you ask Cursor to do something big, it creates something that compilable 
and even runnable, but doesn't even come close to what you want.

For instance, I asked it to port a C# project to Java [LSLib](https://github.com/Norbyte/lslib).  Oh, it converted it to Java.  All the files.  Ran for a
really long time too.  But the output was complete garbage.  It generated a lot of placeholder interfaces and classes even though those classes existed
in the port.  It did work a lot better when I asked it to port a file at a time.  Like with the experience in the previous paragraph though, it either
_sort of_ worked, or didn't work at all.  I'm honestly not quite sure if it would have been faster to manually port LSLib.

What Cursor was completely amazing at was live coding.  While I code, it offers suggestions that you can press the TAB key to use.  It was quite eery 
sometimes how it guessed what I wanted.  I'm completely addicted to this feature and can't live without it.

It was also quite good when I asked it to do small concise things like
* Create me a regular expression to match something
* Create me a function to put a space before any capital letter in text

Stuff like that.

It was also incredible on the UI side.  I asked it to add a menu button that had a pull down menu.  It generated it perfectly.
I asked it to generate a tooltip with HTML I would provide it whenever it hovered over a specific link.  It generated it perfectly.
I asked it to show a red dot over a button whenever a certain counter was above zero.  Not only did it do that, but it added a cool 
effect to the red dot without me even asking.  Again, if you ask it to do specific small concise tasks, it can do it.  Well....
most of the time it can :)  Sometimes, even when you're concise, it outputs garbage.

My opinion is, is that generative AI (for coding and writing) requires constant, repeated human interaction.  It does not replace humans,
it requires our input, constantly, because more often than not, it outputs garbage.  What I liked about Cursor was this back and forth,
between me and the AI was fast and seemless and it allowed me to avoid adding crappy code.
