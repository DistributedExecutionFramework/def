#!/bin/sh

APP_DIR=~/library
PID_FILE=${APP_DIR}/library.pid
JAR=${APP_DIR}/library*-all.jar
STDOUT_LOG=${APP_DIR}/stdout.log

for CONF_BAK in "${APP_DIR}"/*.bak
do
    echo "Restore configuration backup ${CONF_BAK} ..."
    cp "${CONF_BAK}" "${CONF_BAK::-4}"
done
cd ${APP_DIR}
nohup java -XX:+HeapDumpOnOutOfMemoryError -Dlog4j.configurationFile=log4j2.xml -jar ${JAR} >> ${STDOUT_LOG} &
echo $! > ${PID_FILE}
