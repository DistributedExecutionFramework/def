#!/bin/sh
CURRENT_DIRECTORY=$(pwd)
echo $CURRENT_DIRECTORY
#cd $CURRENT_DIRECTORY
mkdir -p /tmp/cluster
rm /tmp/cluster/*
cd cluster
../gradlew shadowJar
cp build/libs/*all*jar /tmp/cluster
cp src/main/resources/* /tmp/cluster
cd /tmp/clusterlibrar