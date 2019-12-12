cmake_minimum_required(VERSION 2.8)
project(base_routines)

set(CMAKE_CXX_STANDARD 98)

add_executable(defaultMapper defaultMapper.cpp RoutineCommunication_types.cpp)
target_link_libraries(defaultMapper thrift)

add_executable(defaultStorer defaultStorer.cpp RoutineCommunication_types.cpp)
target_link_libraries(defaultStorer thrift)
