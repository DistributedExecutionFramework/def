classdef DEFDoubleVector
    
    properties (GetAccess='public', SetAccess='private')
        Value
        JavaObject
    end
    
    methods (Access='public')
        function obj = DEFDoubleVector(value)
            % DEFDouble Construct from a Java obj or real value.
            v = [0];
            if (nargin > 0)
                v = value;
            end
            if isjava(v)
                obj.JavaObject = v;
                obj.Value = cell2mat(v.getValues().toArray().cell)';
            else
                list = DEFDoubleVector.vector2arrayList(v);
                javaObject = at.enfilo.def.datatype.DEFDoubleVector(list);
                obj.JavaObject = javaObject;
                obj.Value = v;
            end
        end
        
        function setValue(obj, value)
            % Update value
            obj.Value = value;
            list = DEFDoubleVector.vector2arrayList(value);
            javaMethod('setValues', obj.JavaObject, list);
        end
    end
    
    methods (Static)
        function arrayList = vector2arrayList(vector)
            arrayList = java.util.ArrayList();
            arrayfun(@(e) arrayList.add(e), vector);
        end
    end
end

