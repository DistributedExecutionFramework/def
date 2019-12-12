#!/bin/sh

for F in `ls *.thrift`
do
    thrift -v --gen java -out src/main/java/ ${F}
    echo ${F} - GENERATED.
done
