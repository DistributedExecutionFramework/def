#!/bin/sh

APP_DIR=~/cluster
PID_FILE=${APP_DIR}/cluster.pid
JAR=${APP_DIR}/cluster*-all.jar
STDOUT_LOG=${APP_DIR}/stdout.log
TOTAL_MEM=$(free -m | grep ^Mem | awk '{ print $2 }')
JAVA_MEM="$((${TOTAL_MEM} / 2))"

for CONF_BAK in "${APP_DIR}"/*.bak
do
    echo "Restore configuration backup ${CONF_BAK} ..."
    cp "${CONF_BAK}" "${CONF_BAK::-4}"
done
echo "Start Cluster ..."
cd ${APP_DIR}
nohup java -Xmx${JAVA_MEM}m -XX:+HeapDumpOnOutOfMemoryError -Dlog4j.configurationFile=log4j2.xml -jar ${JAR} >> ${STDOUT_LOG} &
echo $! > ${PID_FILE}
