classdef ProgramDTO
    % ProgramDTO wrapper
    
    properties (GetAccess='public', SetAccess='private')
        Id
        State
        CreateTime
        FinishTime
        Name
        Description
        NrOfJobs
        JavaObject
    end
    
    methods
        function obj = ProgramDTO(javaObject)
            % ProgramDTO Construct needs java object ProgramDTO.
            obj.JavaObject = javaObject;
            obj.Id = char(javaMethod('getId', javaObject));
            obj.State = char(javaMethod('name', javaMethod('getState', javaObject)));
            obj.CreateTime = datetime(javaMethod('getCreateTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.FinishTime = datetime(javaMethod('getFinishTime', javaObject) / 1000, 'ConvertFrom', 'posixtime', 'TimeZone', 'Europe/Vienna');
            obj.NrOfJobs = javaMethod('getNrOfJobs', javaObject);
            obj.Name = char(javaMethod('getName', javaObject));
            obj.Description = char(javaMethod('getDescription', javaObject));
        end
    end
end

