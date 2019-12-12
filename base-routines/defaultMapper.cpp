#include <iostream>
#include <fstream>

#include "RoutineCommunication_types.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSimpleFileTransport.h>
#include <thrift/stdcxx.h>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace apache::thrift::stdcxx;

using namespace at::enfilo::def::routine::api;

int main(int args, char* argv[]) {
    if (args != 4) {
        std::cerr << "3 Arguments needed: <in-pipe> <out-pipe> <ctrl-pipe>" << std::endl;
        return 1;
    }

    // In Pipe
    shared_ptr<TTransport> inPipe = make_shared<TSimpleFileTransport>(argv[1], true, false);
    shared_ptr<TProtocol> inProto = make_shared<TBinaryProtocol>(inPipe);
    // Out Pipe
    shared_ptr<TTransport> outPipe = make_shared<TSimpleFileTransport>(argv[2], false, true);
    shared_ptr<TProtocol> outProto = make_shared<TBinaryProtocol>(outPipe);
    // Ctrl Pipe
    shared_ptr<TTransport> ctrlPipe = make_shared<TSimpleFileTransport>(argv[3], false, true);
    shared_ptr<TProtocol> ctrlProto = make_shared<TBinaryProtocol>(ctrlPipe);


    try {
        // Open pipes
        inPipe->open();
        outPipe->open();
        ctrlPipe->open();

        // Read #bytes from inPipe
        int32_t bytes = 0;
        inProto->readI32(bytes);

        // Write #tuples to outPipe
        outProto->writeI32(1);
        // Write DEFAULT key to outPipe
        outProto->writeString("DEFAULT");
        // Write #bytes to outPipe
        outProto->writeI32(bytes);
        // Forward struct byte by byte
        for (int i = 0; i < bytes; i++) {
            int8_t b = 0;
            inProto->readByte(b);
            outProto->writeByte(b);
        }
        // Send end signal through ctrl Pipe
        Order* routineDone = new Order();
        routineDone->command = Command::ROUTINE_DONE;
        routineDone->write(ctrlProto.get());

        // Close and flush pipes
        inPipe->close();
        outPipe->flush();
        outPipe->close();
        ctrlPipe->flush();
        ctrlPipe->close();

    } catch (std::exception &ex) {
        std::cerr << "Error: " << ex.what() << std::endl;
        return 1;
    }

    return 0;
}
