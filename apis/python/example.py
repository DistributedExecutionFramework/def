import asyncio

from def_api.client import DEFClient
from def_api.client_helper import RoutineInstanceBuilder, extract_result
from def_api.thrift.communication.ttypes import Protocol
from def_api.thrift.transfer.ttypes import *
from def_api.ttypes import *

# ...

# Setup asyncio for future support.
loop = asyncio.get_event_loop()
# create client
client = DEFClient(host='localhost', port=40012, protocol=Protocol.THRIFT_TCP)

# Create program
future_p_id = client.create_program('cluster1', 'user1')
loop.run_until_complete(future_p_id)
p_id = future_p_id.result()
print("Program: " + p_id)
j = 0

# Create shared resource
future_r_id = client.create_shared_resource(p_id, DEFDouble(value=1e-9))
loop.run_until_complete(future_r_id)
r_id = future_r_id.result()
print("SharedResource: " + r_id)

while j < 2:  # Job loop
    j = j + 1
    # Create job
    future_j_id = client.create_job(p_id)
    loop.run_until_complete(future_j_id)
    j_id = future_j_id.result()
    print("Job: " + j_id)
    t = 0

    while t < 10:  # Task loop
        t = t + 1
        # Prepare routine instance and create a task
        builder = RoutineInstanceBuilder('cfec958c-e34f-3240-bcea-cdeebd186cf6')
        builder.add_shared_resource_parameter('stepSize', r_id)
        builder.add_parameter('start', DEFDouble(value=0))
        builder.add_parameter('end', DEFDouble(value=1e9))
        client.create_task(p_id, j_id, builder.get_routine_instance())

    client.mark_job_as_complete(p_id, j_id)
    print("Wait for job.")
    state = client.wait_for_job_finished(p_id,
                                         j_id)  # Blocking call which waits to job reach the state SUCCESS or FAILED.

    if state == ExecutionState.SUCCESS:
        # Fetch all tasks and task results.
        future_t_ids = client.get_all_tasks(p_id, j_id, SortingCriterion.NO_SORTING)
        loop.run_until_complete(future_t_ids)
        t_ids = future_t_ids.result()
        for t_id in t_ids:
            future_t_info = client.get_task(p_id, j_id, t_id)
            loop.run_until_complete(future_t_info)
            task = future_t_info.result()
            task_result = extract_result(task, DEFDouble())
            print("Task Result: " + str(task_result.value))

    client.delete_job(p_id, j_id)  # Optional

client.mark_program_as_finished(p_id)
client.delete_program(p_id)  # Optional
