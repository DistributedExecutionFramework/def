#!/bin/sh

APP_DIR=~/client-routine-worker
JARS=client-routine-worker*.jar

cd ${APP_DIR}
for JAR in ${JARS}
do
    if [ -f ${JAR} ]
    then
        rm ${JAR}
    fi
done