classdef DEFClient
    %DEFCLIENT Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (GetAccess='public',SetAccess='private')
        JavaClient
    end
    
    methods
        
        function obj = DEFClient(host, port, protocol)
            % Constructor of DEFClient
            
            % Start Java Client
            javaaddpath(fullfile(matlabroot, 'java', 'jar', 'client-api-1.4.6-all.jar'));
            javaaddpath(fullfile(matlabroot, 'java', 'jar', 'base-datatypes-1.4.6-all.jar'));
            endpoint = at.enfilo.def.communication.dto.ServiceEndpointDTO();
            javaMethod('setHost', endpoint, host);
            javaMethod('setPort', endpoint, port);
            proto = javaMethod('valueOf', 'at.enfilo.def.communication.dto.Protocol', protocol);
            javaMethod('setProtocol', endpoint, proto);
            obj.JavaClient = javaMethod('createClient', 'at.enfilo.def.client.api.DEFClientFactory', endpoint);
        end
        
        function future_pIds = getAllPrograms(obj, userId)
            % Find all programs which belongs to given userId
            javaFuture = javaMethod('getAllPrograms', obj.JavaClient, userId);
            future_pIds = Future(javaFuture);
        end
        
        function future_pId = createProgram(obj, clusterId, userId)
            % Create a program
            javaFuture = javaMethod('createProgram', obj.JavaClient, clusterId, userId);
            future_pId = Future(javaFuture);
        end
        
        function future_programDTO = getProgram(obj, pId)
            % Fetch a program
            javaFuture = javaMethod('getProgram', obj.JavaClient, pId);
            future_programDTO = Future(javaFuture, 'ProgramDTO');
        end
        
        function future_deleteProgram = deleteProgram(obj, pId)
            % Delete given Program with all associated jobs and tasks
            javaFuture = javaMethod('deleteProgram', obj.JavaClient, pId);
            future_deleteProgram = Future(javaFuture);
        end
        
        function future_abortProgram = abortProgram(obj, pId)
            % Abort execution of Program (incl. Jobs and Tasks)
            javaFuture = javaMethod('abortProgram', obj.JavaClient, pId);
            future_abortProgram = Future(javaFuture);
        end
        
        function future_updateProgram = updateProgramName(obj, pId, name)
            % Update name of a Program
            javaFuture = javaMethod('updateProgramName', obj.JavaClient, pId, name);
            future_updateProgram = Future(javaFuture);
        end
        
        function future_updateProgram = updateProgramDescription(obj, pId, description)
            % Update description of a Program
            javaFuture = javaMethod('updateProgramDescription', obj.JavaClient, pId, description);
            future_updateProgram = Future(javaFuture);
        end
        
        function future_markProgram = markProgramAsFinished(obj, pId)
            % Mark a Program as finished
            javaFuture = javaMethod('markProgramAsFinished', obj.JavaClient, pId);
            future_markProgram = Future(javaFuture);
        end
        
        function future_createSharedResource = createSharedResource(obj, pId, value)
            % Create a SharedResource in the given Program
            javaFuture = javaMethod('createSharedResource', obj.JavaClient, pId, value.JavaObject);
            future_createSharedResource = Future(javaFuture);
        end
        
        function future_rIds = getAllSharedResources(obj, pId)
            % Returns all list of SharedResoruces which belongs to given
            % Program id
            javaFuture = javaMethod('getAllSharedResources', obj.JavaClient, pId);
            future_rIds = Future(javaFuture);
        end
        
        function future_sharedResource = getSharedResource(obj, pId, rId)
            % Returns the specified SharedResoruces
            javaFuture = javaMethod('getSharedResource', obj.JavaClient, pId, rId);
            future_sharedResource = Future(javaFuture, 'ResourceDTO');
        end
        
        function future_deleteSharedResource = deleteSharedResource(obj, pId, rId)
            % Delete given SharedResource
            javaFuture = javaMethod('deleteSharedResource', obj.JavaClient, pId, rId);
            future_deleteSharedResource = Future(javaFuture);
        end
        
        function future_jIds = getAllJobs(obj, pId)
            % Get all Job Ids of a Program
            javaFuture = javaMethod('getAllJobs', obj.JavaClient, pId);
            future_jIds = Future(javaFuture, 'List');
        end
        
        function future_jId = createJob(obj, pId)
            % Create a Job
            javaFuture = javaMethod('createJob', obj.JavaClient, pId);
            future_jId = Future(javaFuture);
        end
        
        function future_jobDTO = getJob(obj, pId, jId)
            % Fetch a Job
            javaFuture = javaMethod('getJob', obj.JavaClient, pId, jId);
            future_jobDTO = Future(javaFuture, 'JobDTO');
        end
        
        function future_deleteJob = deleteJob(obj, pId, jId)
            % Delete given Job with all Tasks
            javaFuture = javaMethod('deleteJob', obj.JavaClient, pId, jId);
            future_deleteJob = Future(javaFuture);
        end
        
        function future_routineId = getAttachedMapRoutine(obj, pId, jId)
            % Returns attached MapRoutine Id
            javaFuture = javaMethod('getAttachedMapRoutine', obj.JavaClient, pId, jId);
            future_routineId = Future(javaFuture);
        end
        
        function future_attachRoutine = attachedMapRoutine(obj, pId, jId, mapRoutineId)
            % Returns attached MapRoutine Id
            javaFuture = javaMethod('attachedMapRoutine', obj.JavaClient, pId, jId, mapRoutineId);
            future_attachRoutine = Future(javaFuture);
        end
        
        function future_routineId = getAttachedReduceRoutine(obj, pId, jId)
            % Returns attached ReduceRoutine Id
            javaFuture = javaMethod('getAttachedReduceRoutine', obj.JavaClient, pId, jId);
            future_routineId = Future(javaFuture);
        end
        
        function future_attachRoutine = attachedReduceRoutine(obj, pId, jId, reduceRoutineId)
            % Returns attached ReduceRoutine Id
            javaFuture = javaMethod('attachedReduceRoutine', obj.JavaClient, pId, jId, reduceRoutineId);
            future_attachRoutine = Future(javaFuture);
        end
        
        function future_tIds = getAllTasks(obj, pId, jId, sorting)
            % Get all Job Ids of a Program
            sortingCriterion = eval(['at.enfilo.def.transfer.dto.SortingCriterion.' sorting]);
            javaFuture = javaMethod('getAllTasks', obj.JavaClient, pId, jId, sortingCriterion);
            future_tIds = Future(javaFuture, 'List');
        end
        
        function future_tIds = getAllTasksWithState(obj, pId, jId, state, sorting)
            % Get all Job Ids of a Program
            sortingCriterion = eval(['at.enfilo.def.transfer.dto.SortingCriterion.' sorting]);
            execState = eval(['at.enfilo.def.transfer.dto.ExecutionState.' state]);
            javaFuture = javaMethod('getAllTasksWithState', obj.JavaClient, pId, jId, execState, sortingCriterion);
            future_tIds = Future(javaFuture, 'List');
        end
        
        function future_tId = createTask(obj, pId, jId, routineInstance)
            % Create a Task
            routineInstanceDTO = buildRoutineInstanceDTO(routineInstance);
            javaFuture = javaMethod('createTask', obj.JavaClient, pId, jId, routineInstanceDTO.JavaObject);
            future_tId = Future(javaFuture);
        end
        
        function future_taskDTO = getTask(obj, pId, jId, tId, includeInParameters, includeOutParameters)
            % Fetch a Task
            incIn = true;
            incOut = true;
            if nargin > 4
                incIn = includeInParameters;
                incOut = includeOutParameters;
            end
            javaFuture = javaMethod('getTask', obj.JavaClient, pId, jId, tId, incIn, incOut);
            future_taskDTO = Future(javaFuture, 'TaskDTO');
        end
        
        function param = extractOutParameter(obj, task, type, key)
            % Extract out parameter from given task
            k = 'DEFAULT';
            if nargin > 3
                k = key;
            end
            instance = feval(type);
            cls = javaMethod('getClass', instance.JavaObject);
            javaParam = javaMethod('extractOutParameterRaw', obj.JavaClient, task.JavaObject, cls, k);
            param = feval(type, javaParam);
        end
        
        function result = extractReducedResult(obj, job, type, key)
            % Extract reduced result from given task
            k = 'DEFAULT';
            if nargin > 3
                k = key;
            end
            instance = feval(type);
            cls = javaMethod('getClass', instance.JavaObject);
            result = javaMethod('extractReducedResultRaw', obj.JavaClient, job.JavaObject, cls, k);
        end
        
        function future_markComplete = markJobAsComplete(obj, pId, jId)
            % Mark given Job as complete (all tasks created)
            javaFuture = javaMethod('markJobAsComplete', obj.JavaClient, pId, jId);
            future_markComplete = Future(javaFuture);
        end
        
        function future_abortJob = abortJob(obj, pId, jId)
            % Abort given Job 
            javaFuture = javaMethod('abortJob', obj.JavaClient, pId, jId);
            future_abortJob = Future(javaFuture);
        end
        
        function future_abortTask = abortTask(obj, pId, jId, tId)
            % Abort given Task 
            javaFuture = javaMethod('abortTask', obj.JavaClient, pId, jId, tId);
            future_abortTask = Future(javaFuture);
        end
        
        function future_reRunTask = reRunTask(obj, pId, jId, tId)
            % Re run given Task 
            javaFuture = javaMethod('reRunTask', obj.JavaClient, pId, jId, tId);
            future_reRunTask = Future(javaFuture);
        end
        
        function job = waitForJob(obj, pId, jId)
            % Wait for Job until all Tasks are finished (success or error) 
            javaJob = javaMethod('waitForJob', obj.JavaClient, pId, jId);
            job = JobDTO(javaJob);
        end
    end
    
end

