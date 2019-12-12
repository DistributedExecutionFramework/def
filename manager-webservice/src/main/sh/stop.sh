#!/bin/sh

PID_FILE=~/manager-webservice/manager-webservice.pid
CONF_FILES=(~/manager-webservice/manager-webservice.yml ~/manager-webservice/log4j2.xml)

if [ -f "${PID_FILE}" ]
then
	PID=$(cat ${PID_FILE})
	if ps --pid ${PID} > /dev/null
	then
    echo "Manager-Webservice running on PID ${PID}, try to stop it ..."
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
