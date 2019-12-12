#!/bin/sh

APP_DIR=~/worker

for JAR in ${APP_DIR}/worker*jar
do
    if [ -f "${JAR}" ]
    then
        echo "Remove old jar: ${JAR}"
        rm "${JAR}"
    fi
done