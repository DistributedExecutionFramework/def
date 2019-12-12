classdef DEFInteger
    
    properties (GetAccess='public', SetAccess='private')
        Value
        JavaObject
    end
    
    methods
        function obj = DEFInteger(value)
            % DEFInteger Construct from a Java obj or real value.
            v = 0;
            if (nargin > 0)
                v = value;
            end
            if isnumeric(v)
                javaObject = at.enfilo.def.datatype.DEFInteger(v);
                obj.JavaObject = javaObject;
            else
                obj.JavaObject = v;
            end
            obj.Value = javaMethod('getValue', obj.JavaObject);
        end
        
        function setValue(obj, value)
            % Update value
            obj.Value = value;
            javaMethod('setValue', obj.JavaObject, value);
        end
    end
end

