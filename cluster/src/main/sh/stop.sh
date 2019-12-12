#!/bin/sh

PID_FILE=~/cluster/cluster.pid
CONF_FILES=(~/cluster/cluster.yml ~/cluster/log4j2.xml)

if [ -f ${PID_FILE} ]
then
	PID=$(cat ${PID_FILE})
	if ps --pid ${PID} > /dev/null
	then
        echo "Cluster running on PID ${PID}, try to stop it ..."
	    kill ${PID}
	fi
fi

for CONF in ${CONF_FILES[*]}
do
    if [ -f "${CONF}" ]
    then
        echo "Backup configuration ${CONF} ..."
        cp "${CONF}" "${CONF}.bak"
    fi
done
