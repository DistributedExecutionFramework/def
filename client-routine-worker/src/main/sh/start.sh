#!/bin/sh

PID_FILE=~/client-routine-worker/client-routine-worker.pid
APP_DIR=~/client-routine-worker
JAR=client-routine-worker*-all.jar
STDOUT_LOG=~/client-routine-worker/client-routine-worker.log

cd ${APP_DIR}
for CONF_BAK in `ls *.bak`
do
    echo "Restore configuration backup ${CONF_BAK} ..."
    cp ${CONF_BAK} ${CONF_BAK::-4}
done
echo "Start worker ..."
nohup java -XX:+HeapDumpOnOutOfMemoryError -Dlog4j.configurationFile=log4j2.xml -jar ${JAR} >> ${STDOUT_LOG} &
echo $! > ${PID_FILE}
