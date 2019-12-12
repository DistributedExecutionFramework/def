#include <iostream>
#include <fstream>
#include <sstream>

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
    if (args != 3) {
        std::cerr << "2 Arguments needed: <in-pipe> <ctrl-pipe>" << std::endl;
        return 1;
    }

    // In Pipe
    shared_ptr<TTransport> inPipe = make_shared<TSimpleFileTransport>(argv[1], true, false);
    shared_ptr<TProtocol> inProto = make_shared<TBinaryProtocol>(inPipe);
    // Ctrl Pipe
    shared_ptr<TTransport> ctrlPipe = make_shared<TSimpleFileTransport>(argv[2], false, true);
    shared_ptr<TProtocol> ctrlProto = make_shared<TBinaryProtocol>(ctrlPipe);


    try {
        // Open pipes
        inPipe->open();
        ctrlPipe->open();

        // Read number of tuples from inPipe and forward it to ctrlPipe as a Order
        int32_t tuples = 0;
        inProto->readI32(tuples);
        std::stringstream ss;
        ss << tuples;

        Order* sendResults = new Order();
        sendResults->command = Command::SEND_RESULT;
        sendResults->value = ss.str();
        sendResults->write(ctrlProto.get());
        ctrlPipe->flush();

        for (int i = 0; i < tuples; i++) {
            // Create a Result struct for every tuple
            Result* result = new Result();
            result->seq = i;
            // Read key
            std::string key = std::string();
            inProto->readString(key);
            result->key = key;
            // Read #bytes
            int32_t bytes = 0;
            inProto->readI32(bytes);
            result->data = std::string(static_cast<unsigned long>(bytes), ' ');
            for (int j = 0; j < bytes; j++) {
                int8_t b = 0;
                inProto->readByte(b);
                result->data[j] = (char)b;
            }
            result->write(ctrlProto.get());
            ctrlPipe->flush();
            delete result;
        }

        // Send end signal through ctrl Pipe
        Order* routineDone = new Order();
        routineDone->command = Command::ROUTINE_DONE;
        routineDone->write(ctrlProto.get());

        // Close and flush pipes
        inPipe->close();
        ctrlPipe->flush();
        ctrlPipe->close();

    } catch (std::exception &ex) {
        std::cerr << "Error: " << ex.what() << std::endl;
        return 1;
    }

    return 0;
}
