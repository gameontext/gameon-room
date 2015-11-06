#!/bin/bash
export service_concierge=http:\/\/gameon-concierge:9081/concierge/registerRoom
export service_room=ws:\/\/game-on.org/rooms
/opt/ibm/wlp/bin/server run defaultServer

