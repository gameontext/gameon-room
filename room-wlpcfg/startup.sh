#!/bin/bash

if [ "$LOGGING_DOCKER_HOST" != "" ]; then
  /opt/ibm/wlp/bin/server start defaultServer
  echo Starting the logstash forwarder...
  wget https://admin:$ADMIN_PASSWORD@game-on.org:8443/logstashneeds.tar -O /opt/logstashneeds.tar
  sed -i s/PLACEHOLDER_LOGHOST/$LOGGING_DOCKER_HOST/g /opt/forwarder.conf
  cd opt / ; tar xvzf logstashneeds.tar ; rm logstashneeds.tar
  chmod +x ./forwarder ; tar xvzf logstashneeds.tar ; rm logstashneeds.tar
  ./forwarder --config ./forwarder.conf
else
  /opt/ibm/wlp/bin/server run defaultServer
fi