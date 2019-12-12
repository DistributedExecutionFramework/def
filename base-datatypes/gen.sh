#!/bin/sh

for F in `ls src/main/resources/*.thrift`
do
    thrift -v --gen java -out src/main/java/ ${F}
    echo ${F} - GENERATED.
done
