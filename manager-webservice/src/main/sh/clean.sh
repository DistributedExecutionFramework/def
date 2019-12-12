#!/bin/sh

APP_DIR=~/manager-webservice

for JAR in "${APP_DIR}"/manager*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done
