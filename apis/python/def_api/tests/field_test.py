#!/usr/bin/env python
import asyncio
import uuid

from thrift import TSerialization

from def_api.client import DEFClient
from def_api.client_helper import RoutineInstanceBuilder, extract_result
from def_api.json import thrift2json, json2thrift
from def_api.thrift.transfer.ttypes import *
from def_api.ttypes import *


def test():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print("try to run DEFClient")
    #client = DEFClient(host='localhost', port=40000, protocol=Protocol.REST)
    client = DEFClient(host='manager', port=40002, protocol=Protocol.THRIFT_TCP, loop=loop)

    #map_routine_id = '4e339e30-cd45-3101-8bb1-39f18895846a'

    # create program
    future_p_id = client.create_program('cluster1', 'userId')
    #future_p_id = client.create_program('018152b6-d759-4638-97fa-441b0eda7726', 'userId')
    loop.run_until_complete(future_p_id)
    p_id = future_p_id.result()
    future_p_info = client.get_program(p_id)
    loop.run_until_complete(future_p_info)
    print(future_p_info.result())

    # create job
    future_j_id = client.create_job(p_id)
    loop.run_until_complete(future_j_id)
    j_id = future_j_id.result()
    print("job created: " + j_id)
    #client.attach_map_routine(p_id, j_id, map_routine_id)

    # prepare routine_instance
    # PICalc
    builder = RoutineInstanceBuilder('cfec958c-e34f-3240-bcea-cdeebd186cf6')
    builder.add_parameter('start', DEFDouble(value=0))
    builder.add_parameter('end', DEFDouble(value=1e6))
    builder.add_parameter('stepSize', DEFDouble(value=1e-6))
    #builder = RoutineInstanceBuilder('new')
    #builder.add_parameter('samples', DEFDouble(0))
    #builder.add_parameter('origData', DEFDouble(0))
    #builder.add_parameter('scenarioData', DEFDouble(0))


    # create tasks
    #for x in range(0, 20):
    for x in range(0, 10):
        future_t_id = client.create_task(p_id, j_id, builder.get_routine_instance())
        loop.run_until_complete(future_t_id)
        #time.sleep(5)

    client.mark_job_as_complete(p_id, j_id)
    client.wait_for_job_finished(p_id, j_id)
    future_t_ids = client.get_all_tasks(p_id, j_id, ExecutionState.SUCCESS)
    loop.run_until_complete(future_t_ids)
    t_ids = future_t_ids.result()
    for t_id in t_ids:
        future_t_info = client.get_task(p_id, j_id, t_id)
        loop.run_until_complete(future_t_info)
        task = future_t_info.result()
        print(task)
        print(extract_result(task, DEFDouble()))

    client.delete_job(p_id, j_id)
    client.delete_program(p_id)
    loop.close()

def blub():
    j1 = JobDTO()
    j1.id = "id1"
    j1.programId = "id2"
    j1.mapRoutineId = "xxx"
    j1.numberOfTasks = 3555556
    j1.numberOfFinishedTasks = -1
    #j1_json = thrift2json(j1)
    #print(j1_json)
    #j2 = json2thrift(j1_json, JobDTO)
    #print(j1)
    #print(j2)
    p = '{"id":"8c8bb3c5-5378-4e0f-a9c1-1cbdf3b750c7","state":1,"createTime":1502199289569,"finishTime":0,"masterLibraryRoutine":false,"setId":true,"setState":false,"setCreateTime":true,"setFinishTime":true,"setMasterLibraryRoutine":true}'
    print(p)
    pp = json2thrift(p, ProgramDTO)
    #print(pp)
    #rib = RoutineInstanceBuilder('routine_id')
    #rib.add_missing_parameter('param1')
    #rib.add_missing_parameter('param2')
    #rib.add_paramater('param3', 'DEFDouble', DEFDouble())
    #rib.add_paramater('param4', 'DEFDouble', DEFDouble())
    #print(rib.get_routine_instance())
    resource = ResourceDTO()
    resource.id = str(uuid.uuid4())
    resource.key = "key"
    resource.data = TSerialization.serialize(DEFDouble(4))
    r = thrift2json(resource)
    print(r)

if __name__ == '__main__':
    #blub()
    test()
