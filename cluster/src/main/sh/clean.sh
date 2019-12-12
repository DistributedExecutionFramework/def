#!/bin/sh

APP_DIR=~/cluster

for JAR in "${APP_DIR}"/cluster*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done
