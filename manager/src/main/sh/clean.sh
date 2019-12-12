#!/bin/sh

APP_DIR=~/manager

for JAR in "${APP_DIR}"/manager*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done
