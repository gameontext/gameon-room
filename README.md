# gameon-room

This is a service to manage the various rooms available in a game. The primary entry point for a player will be the concierge, who manages connecting you between rooms. After that each room has a room service (heh) and players connect to each service and disconnect from the previous one to move between rooms.

A room is assumed to have a number of labeled exits, but it is up to each concierge instance to decide which room will be on the other side of an exit. This might mean one way doors, warp holes, etc. As the wiring is up the concierge it could be dynamic.

## Set up details

We're using Eclipse with WebSphere Developer Tools for Eclipse (WDT) and Buildship. WDT allows you to handle the LIberty application server and Buildship is used to manage the Gradle projects.

These instructions are still in note form and need fleshing out. If something isn't clear please let us know in our WASdev forums.

After you clone the Git repo, you need to import by going to the Java EE perspective in Eclipse and selecting File->Import->Gradle. Trying to Import projects from the Git perspective doesn't seem to provide the same options.

After the import is complete open the Gradle Tasks window and double click the eclipse task in the top level project. This will configure the Eclipse projects for you.

Add a Liberty server, and set the User directory to be the room-wlpcfg project (you may have to go into Advanced settings on the Runtime Environment to do this).

You can then start the server from WDT, and run a build from gameon-room, and the WAR file should be deployed to the correct location.

## Docker for Room App

To build a Docker image for this app/service, execute the following:

```
gradle buildImage
```

Or, if you don't have gradle, then:

```
./gradlew buildImage
```

### Interactive Run

```
docker run -it -p 9080:9080 --env-file=dockerrc gameon-room bash
```

Then, you can start the server with 
```
/opt/ibm/wlp/bin/server run defaultServer
```

### Daemon Run

```
docker run -d -p 9080:9080 --env-file=dockerrc --name gameon-room gameon-room
```

### Stop

```
docker stop gameon-room ; docker rm gameon-room
```

### Restart Daemon

```
docker stop gameon-room ; docker rm gameon-room ; docker run -d -p 9080:9080 --env-file=dockerrc --name gameon-room gameon-room 
```

## Docker for Concierge App

To build a Docker image for this app/service, execute the following:

```
gradle buildImage
```

Or, if you don't have gradle, then:

```
./gradlew buildImage
```

### Interactive Run

```
docker run -it -p 9081:9081 -e LICENSE=accept gameon-concierge bash
```

Then, you can start the server with 
```
/opt/ibm/wlp/bin/server run defaultServer
```

### Daemon Run

```
docker run -d -p 9081:9081 -e LICENSE=accept --name gameon-concierge gameon-concierge
```

### Stop

```
docker stop gameon-concierge ; docker rm gameon-concierge
```

### Restart Daemon

```
docker stop gameon-concierge ; docker rm gameon-concierge ; docker run -d -p 9081:9081 -e LICENSE=accept --name gameon-concierge gameon-concierge
```
