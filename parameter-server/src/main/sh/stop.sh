#!/bin/sh

PID_FILE=~/parameter-server/parameter-server.pid
CONF_FILES=(~/parameter-server/parameter-server.yml ~/parameter-server/log4j2.xml)

if [ -f ${PID_FILE} ]
then
	PID=`cat ${PID_FILE}`
	ps --pid ${PID} > /dev/null
	if [ "$?" == "0" ]
	then
        echo "Parameter Server running on PID ${PID}, trying to stop it ..."
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
