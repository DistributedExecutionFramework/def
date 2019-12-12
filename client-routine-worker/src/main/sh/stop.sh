#!/bin/sh

PID_FILE=~/client-routine-worker/client-routine-worker.pid
CONF_FILES=( ~/client-routine-worker/client-routine-worker.yml ~/client-routine-worker/log4j2.xml )

if [ -f ${PID_FILE} ]
then
	PID=`cat ${PID_FILE}`
	ps --pid ${PID} > /dev/null
	if [ "$?" == "0" ]
	then
        echo "Worker running on PID ${PID}, try to stop it ..."
	    kill ${PID}
	fi
fi

for CONF in ${CONF_FILES[*]}
do
    if [ -f ${CONF} ]
    then
        echo "Backup configuration ${CONF} ..."
        cp ${CONF} ${CONF}.bak
    fi
done
