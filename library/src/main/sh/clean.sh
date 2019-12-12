#!/bin/sh

APP_DIR=~/library

for JAR in "${APP_DIR}"/library*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done
