for %%f in (*.thrift) do (%THRIFT_HOME%/thrift -v --gen java -out src/main/java/ %%f & echo %%f - GENERATED.)
