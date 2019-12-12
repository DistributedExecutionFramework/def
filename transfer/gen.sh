#!/bin/sh
for F in *.thrift
do
    thrift -v --gen java:beans,private-members -out src/main/java/ ${F}
    #thrift -v --gen py -out python/ ${F}
    #thrift -v --gen csharp -out csharp/ ${F}
    echo "${F} - GENERATED."
done
