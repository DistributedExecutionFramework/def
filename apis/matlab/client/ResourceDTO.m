classdef ResourceDTO
    % ResourceDTO wrapper
    
    properties (GetAccess='public', SetAccess='private')
        Id
        DataTypeId
        Data
        Key
        Url
        JavaObject
    end
    
    methods
        function obj = ResourceDTO(javaObject)
            % ResourceDTO Construct needs java object ResourceDTO.
            obj.JavaObject = javaObject;
            obj.Id = char(javaMethod('getId', javaObject));
            obj.DataTypeId = char(javaMethod('getDataTypeId', javaObject));
            obj.Data = javaMethod('getData', javaObject);
            obj.Key = char(javaMethod('getKey', javaObject));
            obj.Url = char(javaMethod('getUrl', javaObject));
        end
    end
end

