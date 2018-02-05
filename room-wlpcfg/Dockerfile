FROM gameontext/docker-liberty-custom

ENV SERVERDIRNAME room

COPY ./startup.sh /opt/startup.sh
ADD ./servers/gameon-room /opt/ol/wlp/usr/servers/defaultServer/

CMD ["/opt/startup.sh"]

HEALTHCHECK \
  --timeout=10s \
  --start-period=40s \
  CMD wget -q -O /dev/null http://localhost:9080/
