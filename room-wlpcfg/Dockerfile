FROM websphere-liberty:latest

MAINTAINER Ben Smith

COPY ./servers/gameon-room/* /opt/ibm/wlp/usr/servers/defaultServer/
COPY ./servers/gameon-room/apps/room-app-1.0.war /opt/ibm/wlp/usr/servers/defaultServer/apps/room-app.war

CMD ["/opt/ibm/wlp/bin/server", "run", "defaultServer"]