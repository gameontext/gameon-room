#!/bin/bash

#
# This script is only intended to run in the IBM DevOps Services Pipeline Environment.
#

#!/bin/bash
echo Informing slack...
#curl -X 'POST' --silent --data-binary '{"text":"A new build for the room service has started."}' $WEBHOOK > /dev/null

mkdir dockercfg ; cd dockercfg
echo Downloading Docker requirements..
wget http://docker-2.game-on.org:8081/dockerneeds.tar -q
echo Setting up Docker...
tar xzf dockerneeds.tar
cd .. 
	 
echo Downloading Java 8...
wget http://game-on.org:8081/jdk-8u65-x64.tar.gz -q
echo Extracting Java 8...
tar xzf jdk-8u65-x64.tar.gz
echo And removing the tarball...
rm jdk-8u65-x64.tar.gz
export JAVA_HOME=$(pwd)/jdk1.8.0_65
# echo $JAVA_HOME
echo Building projects using gradle...
./gradlew build
echo Building and Starting Concierge Docker Image...
cd concierge-wlpcfg
../gradlew buildDockerImage removeCurrentContainer
../gradlew startNewContainer
echo Building and Starting Room Docker Image...
cd ../room-wlpcfg
../gradlew buildDockerImage removeCurrentContainer
../gradlew startNewContainer
echo Removing JDK, cause there's no reason that's an artifact...
cd ..
rm -rf jdk1.8.0_65