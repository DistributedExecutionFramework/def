classdef ObjectiveRoutine
    
    properties (GetAccess='private', SetAccess='private')
        RoutineName
        JavaParameterController
    end
    
    methods (Access = private)
        function obj = ObjectiveRoutine(routineName, inPipe, outPipe, ctrlPipe)
            javaaddpath(fullfile(matlabroot, 'java', 'jar', 'routine-api-matlab-1.4.5-all.jar'));
            javaaddpath(fullfile(matlabroot, 'java', 'jar', 'base-datatypes-1.4.5-all.jar'));
            obj.JavaParameterController = at.enfilo.def.routine.MatlabParameterManager(routineName, inPipe, outPipe, ctrlPipe);
            obj.RoutineName = routineName;
        end
        
        function result = runRoutine(obj)
            result = feval(obj.RoutineName, obj);
        end
        
        function setResult(obj, result)
            javaMethod('setResult', obj.JavaParameterController, result.JavaObject);
        end
    end
    methods
        function p = loadParameter(obj, paramName, paramType)
            javaObj = javaMethod('getParameter', obj.JavaParameterController, paramName, paramType);
            p = feval(paramType, javaObj);
        end
    end
    
    methods (Static, Hidden)
        function createObjectiveRoutine(routineName, inPipe, outPipe, ctrlPipe)
            obj = ObjectiveRoutine(routineName, inPipe, outPipe, ctrlPipe);
            result = obj.runRoutine();
            obj.setResult(result);
        end
    end
end
