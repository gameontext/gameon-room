FROM websphere-liberty:latest

MAINTAINER Ben Smith

ADD ./servers/gameon-concierge /opt/ibm/wlp/usr/servers/defaultServer/

CMD ["/opt/ibm/wlp/bin/server", "run", "defaultServer"]

EXPOSE 9081
