classdef List
    % Java List Wrapper
    
    properties (GetAccess='public', SetAccess='private')
        Items
    end
    
    methods
        function obj = List(javaObject)
            % List
            obj.Items = {};
            it = javaMethod('iterator', javaObject);
            while javaMethod('hasNext', it)
                obj.Items = [obj.Items, char(javaMethod('next', it))];
            end
        end
    end
end

