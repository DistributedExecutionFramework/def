classdef JobDTO
    % JobDTO wrapper
    
    properties (GetAccess='public', SetAccess='private')
        Id
        ProgramId
        State
        CreateTime
        StartTime
        FinishTime
        ScheduledTasks
        RunningTasks
        SuccessfulTasks
        FailedTasks
        MapRoutineId
        ReduceRoutineId
        ReducedResults
        JavaObject
    end
    
    methods
        function obj = JobDTO(javaObject)
            % JobDTO Construct needs java object JobDTO.
            obj.JavaObject = javaObject;
            obj.Id = char(javaMethod('getId', javaObject));
            obj.ProgramId = char(javaMethod('getProgramId', javaObject));
            obj.State = char(javaMethod('name', javaMethod('getState', javaObject)));
            obj.CreateTime = datetime(javaMethod('getCreateTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.StartTime = datetime(javaMethod('getStartTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.FinishTime = datetime(javaMethod('getFinishTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.ScheduledTasks = javaMethod('getScheduledTasks', javaObject);
            obj.RunningTasks = javaMethod('getRunningTasks', javaObject);
            obj.SuccessfulTasks = javaMethod('getSuccessfulTasks', javaObject);
            obj.FailedTasks = javaMethod('getFailedTasks', javaObject);
            obj.MapRoutineId = char(javaMethod('getMapRoutineId', javaObject));
            obj.ReduceRoutineId = char(javaMethod('getReduceRoutineId', javaObject));
            % TODO: ReducedResults
        end
    end
end

