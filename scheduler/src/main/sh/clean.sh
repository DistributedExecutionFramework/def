#!/bin/sh

APP_DIR=~/scheduler

for JAR in "${APP_DIR}"/scheduler*jar
do
    if [ -f "${JAR}" ]
    then
        rm "${JAR}"
    fi
done
