#!/bin/sh

PID_FILE=~/parameter-server/parameter-server.pid
APP_DIR=~/parameter-server
JAR=parameter-server*-all.jar
STDOUT_LOG=~/parameter-server/parameter-server.log

cd ${APP_DIR}
for CONF_BAK in `ls *.bak`
do
    echo "Restore configuration backup ${CONF_BAK} ..."
    cp ${CONF_BAK} ${CONF_BAK::-4}
done
nohup java -XX:+HeapDumpOnOutOfMemoryError -Dlog4j.configurationFile=log4j2.xml -jar ${JAR} >> ${STDOUT_LOG} &
echo $! > ${PID_FILE}
