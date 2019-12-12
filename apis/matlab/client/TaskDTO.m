classdef TaskDTO
    % TaskDTO wrapper
    
    properties (GetAccess='public', SetAccess='private')
        Id
        JobId
        ProgramId
        State
        CreateTime
        StartTime
        FinishTime
        ObjectiveRoutineId
        MapRoutineId
        InParameters
        OutParameters
        Messages
        JavaObject
    end
    
    methods
        function obj = TaskDTO(javaObject)
            % TaskDTO Construct needs java object TaskDTO.
            obj.JavaObject = javaObject;
            obj.Id = char(javaMethod('getId', javaObject));
            obj.JobId = char(javaMethod('getJobId', javaObject));
            obj.ProgramId = char(javaMethod('getProgramId', javaObject));
            obj.State = char(javaMethod('name', javaMethod('getState', javaObject)));
            obj.CreateTime = datetime(javaMethod('getCreateTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.StartTime = datetime(javaMethod('getStartTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.FinishTime = datetime(javaMethod('getFinishTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.ObjectiveRoutineId = char(javaMethod('getObjectiveRoutineId', javaObject));
            obj.MapRoutineId = char(javaMethod('getMapRoutineId', javaObject));
            obj.InParameters = containers.Map();
            obj.OutParameters = [];
            obj.Messages = [];
        end
    end
end

