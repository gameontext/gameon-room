FROM gameontext/docker-liberty-custom:master-29

ENV SERVERDIRNAME room

ADD https://raw.githubusercontent.com/gameontext/gameon/master/bin/gen-keystore.sh /opt/gen-keystore.sh
USER 0
RUN chmod g+rwx /opt/gen-keystore.sh
COPY ./servers/gameon-room /opt/ol/wlp/usr/servers/defaultServer/
RUN mkdir -p /opt/ol/wlp/usr/servers/defaultServer/resources/security && chown -R 1001:0 /opt/ol/wlp/usr/servers/defaultServer && chmod -R g+rwx /opt/ol/wlp/usr/servers/defaultServer
USER 1001
COPY ./startup.sh /opt/startup.sh

CMD ["/opt/startup.sh"]

HEALTHCHECK \
  --timeout=10s \
  --start-period=40s \
  CMD wget -q -O /dev/null http://localhost:9080/rooms/health
