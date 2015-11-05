#!/bin/bash
export DOCKERHOST=$(route -n | grep 'UG[ \t]' | awk '{print $2}')
echo Found Docker host: $DOCKERHOST
export service_concierge=http:\/\/$DOCKERHOST:9081/concierge/registerRoom
export service_room=ws:\/\/$DOCKERHOST:9081/rooms
/opt/ibm/wlp/bin/server run defaultServer

