# Game On! Room Service

[![Codacy Badge](https://api.codacy.com/project/badge/grade/0c29c501ba11477f944e109b85817593)](https://www.codacy.com/app/gameontext/gameon-room)

See the room service [information page](https://gameontext.gitbooks.io/gameon-gitbook/content/microservices/room.html) in the Game On! Docs for more information on how to use this service.

---

The engine provides basic grammar parsing, with an extensible framework for commands, and has concepts of player inventory, room inventory, and allows nested items (containers), it's designed so that all that is a framework, and allows the item to provide the logic for behaviors like 'is the user allowed to access this container'.. and 'what description should be given for this item for this player' .. and 'what happens if the player uses this item (optionally with other items) .. 

It does all that in a way where the framework is kept separate from the room/item/command that's being written, allowing new rooms to be created very quickly. 

The engine was mostly created early in Game On's lifecycle, while a lot of things were changing quite frequently, so it has a clean separation between its own logic, and the the way it passes messages to/from the Room abstraction layer. The Engine is written to allow pluggable abstractions through which it can communicate, one of which is Game On Room Protocol based, and it comes with a debug one that's System in/out based, allowing for easy testing of room logic locally by running the entire engine in the Console. 

The classes are grouped by package/functionality.. 

`net.wasdev.gameon.room`
  is the layer that links to Game On via the Room interface, in there you'll find the stuff that 
   - manages registering the rooms directly with map (and updating their registrations if out of date), 
   - Provides the `RoomResponseProcessor` implementation that links to WebSocket Sessions (pluggable communication abstraction implementation)
   - links `RoomWS` websockets, to Engine `Room` implementations via dynamic Websocket registration per room.
   
`net.wasdev.gameon.room.engine`
is the Engine that provides all the framework for hosting rooms.. Key classes are 
 - `Engine` - can be run as a java class directly to test room logic, is the main handle to the whole Room hosting framework.. 
 - `Room` - contains the `RoomResponseProcessor` implementation that links to System in/out as it's default, overridden by the GameOn Room layer when it links rooms to websockets>
 
`net.wasdev.gameon.room.sample`
is the sample set of rooms built for Game ON's first conference outing, all the rooms today are defined in `SampleDataProvider`.. have a quick read of that to understand how Doors/Rooms/Commands/Items are hooked together to create RecRoom/MugRoom/Basement.  It's insanely quick to add new commands to a Room, add new Rooms, and add new Items, and give them behavior.. The sub packages of `commands` and `items` are as you would expect, the implementations of the commands and items for the content. 

> Note that instance equality is used throughout the 'sample' implementation for object equivalence, this sample is intended to run as a single instance (it allows the quantum state tunneling behavior in-game for the mug in two rooms at once.. and makes the code a lot easier to read) it could be upgraded to have instance id's for each item then use instance equality to test etc, but all that was a layer just not needed for the purpose of this sample. The linkage for most of that equivalence is managed through the simple `Items` class which defines a single instance of each Item that the Sample is using, as a `public static final`so all the other places are able to refer to the same instance. Eg, when `SampleDataProvider` builds MugRoom, it's able to say `new ItemDesc[] {Items.mug, Items.mugRoomSign}`

The Engine is built around the concept of Meta Objects that have their values and logic inserted into them, like abstract objects, but without the use of concrete classes. The intent here was to allow something to pull all the metadata required to implement an item/command etc out into something serializable as text, so room content could be written totally outside java. This becomes a little more hairy as you try to integrate the fact that simple logic is required for stuff like 'what happens when you use X (with Y)'
The Meta Objects live over in `net.wasdev.gameon.room.engine.meta` are all named SomethingDesc, and provide a broad variety of ways to construct them so they gain sensible defaults, and then ways for consumers of an instantiated Desc to interact through the shell to what it is representing. This means the 'Room' class in the Engine can focus on managing all the boilerplate stuff about being a Room in a Text Adventure.. while deferring all the actual content based calls through it's contained `RoomDesc`, which in turn uses the information it was populated with to supply those answers. In a way, it may help to think that the SomethingDesc classes are a solution to avoiding having to dynamically write Java classes (extending an abstract parent) for each Something.. 

The Parser has a tough job, it's asked to parse the content of user input for a room, where because everything is built in the framework  the commands, and items are all not fixed entities, it's not a great parser, but it has an interesting solution. Commands must extend `CommandHandler` from the Parser, which requires every command to implement a few things, most of which are straightforward (help text, should the command show up when /help is listed, the actual logic for the command itself) .. but some are a little more fun.. 

To help the parser have an easier life (and also with forethought over how we might one day do items as a service) .. Commands return a set of `CommandTemplate`s . These are basically patterns the parser can match with to know if input is supposed to be processed by a given CommandHandler. An example might be.. 

```CommandTemplate takeItemInRoom = new CommandTemplateBuilder().build(Type.VERB, "Take").build(Type.ROOM_ITEM).build();```

Which you can loosely read as _"a command, where the first word must be TAKE, with an argument of an item that currently exists in the room"_ pretty straight forward, but you can also do things like.. 

```CommandTemplate takeItemFromContainer = new CommandTemplateBuilder().build(Type.VERB, "Take")        .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "from").build(Type.CONTAINER_ITEM).build();```

Which reads basically as _"a command where the first word must be TAKE, with an argument of any item in a container, followed by the word "FROM" followed by any item that is container"_

By having each command define the templates it supports, the parser is able to know when to invoke the `processCommand` logic for any command that is active within the room the player issued the input string from. 

Each CommandTemplate has a key (actually just a string, the last example would be something like `/V:TAKE/B:/L:FROM/C:`), and when the Parser invokes processCommand, it will pass the key of the template matched (Commands can have multiple templates, eg the take example has both of the examples here, and one more).. This allows the Command implementation to know which variant of itself has been invoked, and then allows it to make sense of the already parsed arguments that are supplied to it. This really helps keep the logic in the commands be only the logic about the command, and not lots of stuff about figuring out parsing of input. 

All this is kinda awesome, until you hit 'USE' as a command, where suddenly the logic for the invocation isn't really supplied by the Use command at all, but is really something the item being used should own.. So, just like Commands can be treated as CommandHandlers, Items can have a ItemUseHandler, and if they do, the Use command implementation is able to throw the request back to the Parser, and say "Hey, can you invoke this UseHandler for me using the rest of this command". So if you check out any of the sample items, say "Stilettos" for example, you'll see an ItemUseHandler being defined, using the same CommandTemplate stuff as regular commands do.. 

Lastly, both Commands and ItemUseHandlers allow implementation of a 'processUnknown' method which the Parser will invoke if the Parser knows this is the right object to handle this input, but it couldn't find a matching template to invoke. This enables the processCommand logic to not have to deal with commands it doesn't understand how to handle, all that can be dealt with in a separate method. 

There are a couple more tricks hidden in there that are kind of fun.. it's worth noting that Items are built to allow a single string for state, which can be atomically manipulated, and that players actually have no state at all beyond their inventory. This is a natural fit for something like Mug when you use Mug with Coffee Machine, the Mug state is either full or empty, and responses to examine, or use will change depending on the Mug state. It's somewhat less of a fit for the Cupboard / Fuse puzzle, where instead of the player having the special state that allows them to access the cupboard, the cupboard peeks into the users inventory to see if they are holding the item that allows them to access its content, and then also checks the state on that item to ensure it has been set by the user performing the right interaction with the item previously. (Kinda tried to avoid spoilers here if you haven't solved it yet ;p )

It may be worth noting also that Cupboard is an instance of a ContainerDesc which extends ItemDesc, so it can provide an AccessHandler, which is tested by the TAKE command, and EXAMINE command to know if the player is allowed to perform that action. 

---

The general idea with the entire project is that the sample folder can be swapped out to drop in a set of rooms with entirely different content.. it's a great way to setup rooms etc, sadly however it does not really help much with Game On beyond just providing fun content. So it's not something we've ever pushed very hard. 

It should be possible to turn the entire project without 'Sample' into a jitpack style library, where you could have simple github projects that just contain the equivalent of the sample package.. 

It may seem odd this room is so far ahead, and yet we've not done much with it, but by driving a room this far, you get a feel for what would be required to implement things like;
 -  a parser service, to free all other rooms from having to write yet another string interpreter.. 
 -  an inventory service, to allow items to be carried between rooms.. (the inventory part is easy, but handling how/if `/use` should function on a foreign object in a room is hard)
 -  where state should live for items / players
 -  admin status.. (check the `/reset` command in RecRoom)
And having that understanding is half the battle to deciding what you should work on next =)



## Contributing

Want to help! Pile On! 

[Contributing to Game On!](CONTRIBUTING.md)
