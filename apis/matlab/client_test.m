clear java;
addpath('client');
addpath('datatypes');

% Create DEF client instance
client = DEFClient('localhost', 40012, 'THRIFT_TCP');

% Create a new Program
future_pId = createProgram(client, 'cluster1', 'user1');
pId = get(future_pId);
disp(pId);

future_rId = createSharedResource(client, pId, DEFDouble(10^-8));
rId = get(future_rId);
disp(rId);

% Create a new Job
future_jId = createJob(client, pId);
jId = get(future_jId);
disp(jId);

% for 
% Create a new Task
% routineInstance = RoutineInstanceBuilder('cfec958c-e34f-3240-bcea-cdeebd186cf6');
routineInstance = RoutineInstanceBuilder('cfec958c-e34f-3240-bcea-cdeebd186cf6');
addParameter(routineInstance, 'start', DEFDouble(0));
addParameter(routineInstance, 'end', DEFDouble(10^8));
addParameter(routineInstance, 'stepSize', rId);
future_tId = createTask(client, pId, jId, routineInstance);
tId = get(future_tId);
disp(tId);
% end for


% Mark job as complete
markJobAsComplete(client, pId, jId);

% ----------------------------------

% Wait for Job
waitForJob(client, pId, jId);

% iterate over tId's
% Fetch single Task
future_task = getTask(client, pId, jId, tId);
task = get(future_task);
if strcmp(task.State, 'SUCCESS')
    result = extractOutParameter(client, task, 'DEFDouble');
    disp(result.Value);
else
    disp(task.State);
end
% end iterator

% Delete all resources
deleteJob(client, pId, jId);
deleteProgram(client, pId);
