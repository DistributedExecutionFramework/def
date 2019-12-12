classdef RoutineInstanceBuilder
    % Builder for RoutineInstance
    
    properties (GetAccess = 'public', SetAccess = 'private')
        RoutineId
        InputParameters
    end
    
    methods
        function obj = RoutineInstanceBuilder(routineId)
            obj.RoutineId = routineId;
            obj.InputParameters = containers.Map();
        end
        
        function addParameter(obj, name, value)
            % Adds a Parameter to this RoutineInstance
            obj.InputParameters(name) = value;
        end
        
        function addSharedResourceParameter(obj, name, rId)
            % Adds a SharedResource as Parameter to this RoutineInstance
            obj.InputParameters(name) = rId;
        end
        
        function dto = buildRoutineInstanceDTO(obj)
            builder = at.enfilo.def.client.api.RoutineInstanceBuilder(obj.RoutineId);
            for k = keys(obj.InputParameters)
                paramName = char(k);
                tmp = obj.InputParameters(paramName);
                if isobject(tmp)
                    value = tmp.JavaObject;
                else
                    value = tmp;
                end
                javaMethod('addParameter', builder, paramName, value);
            end
            dto = RoutineInstanceDTO(javaMethod('build', builder));
        end
    end
end

