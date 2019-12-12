#!/bin/sh
for F in `ls *.thrift`
do
    thrift -v --gen java -out src/main/java/ ${F}
    #thrift -v --gen py -out python/ ${F}
    #thrift -v --gen csharp -out csharp/ ${F}
    echo "${F} - GENERATED."
done