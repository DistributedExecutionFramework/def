#!/bin/sh

for F in `ls *.thrift`
do
    thrift -v --gen java -out src/main/java/ ${F}
    #thrift -v --gen py -out python/ ${F}
    echo ${F} - GENERATED.
done
