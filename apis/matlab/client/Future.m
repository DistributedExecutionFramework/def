classdef Future
    % java.util.concurrent.Future Wrapper
    
    properties (SetAccess='private', GetAccess='private')
        JavaFuture
        WrapResult
        ResultWrapperClass
    end
    
    methods
        function obj = Future(javaFuture, resultWrapperClass)
            if nargin > 1
                obj.WrapResult = true;
                obj.ResultWrapperClass = resultWrapperClass;
            else
                obj.WrapResult = false;
            end
            obj.JavaFuture = javaFuture;
        end
        
        function done = isDone(obj)
            done = javaMethod('isDone', obj.JavaFuture);
        end
        
        function value = get(obj)
            javaObj = javaMethod('get', obj.JavaFuture);
            if obj.WrapResult
                value = feval(obj.ResultWrapperClass, javaObj);
            else
                if ischar(javaObj)
                    value = javaObj;
                else
                    value = char(javaObj);
                end
            end
        end
    end
end

