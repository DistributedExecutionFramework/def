#!/bin/sh

APP_DIR=~/parameter-server
JARS=parameter-server*.jar

cd ${APP_DIR}
for JAR in ${JARS}
do
    if [ -f ${JAR} ]
    then
        rm ${JAR}
    fi
done
