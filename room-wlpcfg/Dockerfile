FROM websphere-liberty:latest

MAINTAINER Ben Smith

ADD ./servers/gameon-room /opt/ibm/wlp/usr/servers/defaultServer/
RUN rm /opt/ibm/wlp/usr/servers/defaultServer/bootstrap.properties

COPY ./startup.sh /opt/startup.sh

CMD ["/opt/startup.sh"]