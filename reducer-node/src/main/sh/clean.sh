#!/bin/sh

APP_DIR=~/reducer

for JAR in "${APP_DIR}"/reducer*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done