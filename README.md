# Baldur's Forge
Project that provides tools and services for Baldur's Gate 3

## Baldur's Armory

AI Chatbot for Baldur's Gate 3.  Search the item DB extracted from Baldur's Gate 3.  Import items from other mods to the DB.  All through an AI chat interface.  
Create new equipment mods from this chatbot too.

## Baldur's Archivist

Java utility library for dealing with various BG3 file formats:  .pkg, .lsf, .lsx, etc.


# Why did I create this project?

* I wanted to create a pure AI Chatbot interface to a CRUD application.  I wanted a semi-real usecase, I was playing BG3 a bunch and thought
hey, a BG3 natural language search and mod creator chatbot might be good.
* I wanted to relearn JQuery and CSS
* I wanted to seriously test drive Cursor AI IDE

# Lessons Learned

The most frustrating thing about developing an AI chatbot was how wildly inconsistent it was.  Stuff that would work one day (or one week, or one month even!)
would not work the next.  This caused me to constantly refine how I was working with it.  This was most of the work: getting consistent and deterministic results from the AI.  
It could be the model I was using (OpenAI 4o).  I will eventually try other models.  My $5 bet?  That this application will not work with another model at all :)

Here's more specific lessons learned:

## Keep prompts specific

## Provide examples with example input and output.  Or example input and what action you want the AI to take

The MainMenuCommands toolbox did not work until I provided examples of input and actions to take based on that input.  Without
those example, the AI was really inconsistent.  Sometimes it would call a tool, sometimes it wouldn't.

## You cannot trust the AI to provide consistently formatted output.

I honestly gave up on trying to get the AI to output HTML.  I even tried returning HTML output from tool methods
and the AI would just ignore it, interpret the response from the tool and output anything it wanted.  No matter what I put in the
prompt, OpenAI would almost always return Markdown.  I was able to get it to output strict json schema, but sometimes it would return
any json embedded within Markdown.  It was very frustrating

## Need to communicate session state between tools and client

For the, browser clients and server-side prompts and tool methods to even be able to work, I needed a way to pass session
information between them.  Chat memory just wasn't enough.  In fact, chat memory often caused problems (see other issues).

I called this a ChatContext and it could hold state that could be passed between the client and server and between tools spanning
multiple calls.  I'll explain more about this in architecture section.

## Your client code should format complex visual responses itself

If you want a nice interface with complex output that is formatted really nicely, you _WILL NOT_
be able to use the AI as-is.  You'll see in the architecture explanation that
invoked tool methods would append additional typed messages to a context object that was sent to the client
and the client would format thing based on rich data structure created by my code.

## AI responses will be inconsistent.  You cannot guarantee what the AI will output

I originally had my search return multiple items in a json array and I'd ask the AI to list and summarize the search.
Sometimes it would provide a numbered list.  Sometimes bulleted.  Sometimes it would produce a paragraph with the names
of things that it found. Thus, I couldn't create a consistent and nice looking UI.  I gave up trying to get the AI to produce
nice output.

## void tool responses confuse the AI

For instance, I had a tool method `void listEquipment()`  It would invoke the tool correctly, but then output
that it could not find anything to list even though I would tell the prompt and tell description not to output anything
when `listEquipment` was called.

## You can't guarantee what the AI will send to tool parameters

For example, I had a search tool method that took a string query parameter.  The AI would look at the user message and extract
keywords before calling the search method.  This would screw up search results.  I had to manually make the raw user message
available to the tool function.  Context matters and keywords can lose context.

Here's a better example:

This happened also with the BoostBuilderChat. I would call it with setBoost/addBoost tool methods.  I would have a prompt of

"create new longsword with boost of +3"

Only "+3" would be sent to the boost prompt and it would not know if it was armor or a weapon and didn't know whether to 
output AC(3) or WeaponEnchantment(3).
Again, I had to send get access to the original user message and send that to the BoostBuilderChat.

## Chat memory can confuse the AI

When I invoked the BoostBuilderChat, I would recycle the memory id of the conversation.  The 2nd time add boost was called
the BoostBuilderChat would convert the user message, but *ALSO* would look in chat history and convert a previous add boost user message request.

User:  add boost advantage on saving throws
AI: Ai would return Advantage(SavingThrows) from BoostBuilderChat call
User:  add boost +3 weapon enchantment
AI:  Ai would return Advantage(SavingThrows);WeaponEnchantment(3)

To solve this, I removed the @MemoryId parameter to the boost tool method.

## You can't guarantee that the AI will call a tool

I used to have BoostBuilderChat as part of the @ToolBox for WeaponBuilderChat.buildWeapon (and the other builders).  For a long long time
it would consistently convert a boost description to a boost macro by calling BoostBuilderChat before invoking setBoost/addBoost tool methods.
*Then it just stopped working consistently!!!*  For no reason at all OpenAI would or would not call the boost builder chat. It was different every time.


# Architecture

## ChatContext

## Stateless Chat Memory

## Context UI Events

## Main Menu Chat

## Find vs. Search

## RAG with Metadata

## Chat Frames

## Propagating User Message

## Building a JSON Document with Chat

## Thrown Out Experiment:  Chat Commands

## Unused Langchain4j constructs

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
