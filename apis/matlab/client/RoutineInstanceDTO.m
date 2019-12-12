classdef RoutineInstanceDTO
    % RoutineInstanceDTO wrapper
    
    properties (GetAccess='public', SetAccess='private')
        RoutineId
        InParameters
        MissingParameters
        JavaObject
    end
    
    methods
        function obj = RoutineInstanceDTO(javaObject)
            % RoutineInstanceDTO Construct needs java object RoutineInstanceDTO.
            obj.JavaObject = javaObject;
            obj.RoutineId = char(javaMethod('getRoutineId', javaObject));
            obj.InParameters = containers.Map();
            obj.MissingParameters = {};
            
        end
    end
end

