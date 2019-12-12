%% Map types between DEF specific datatype and MATLAB
function value = map(value)
    if isjava(value)
        % Map DEF Datatype to a MATLAB type
        switch class(value)
            case 'at.enfilo.def.datatype.DEFBinary'
                value = javaMethod('getValue', value);
            
            % Scalar values
            case 'at.enfilo.def.datatype.DEFBoolean'
                value = javaMethod('getValue', value);
            case 'at.enfilo.def.datatype.DEFDouble'
                value = javaMethod('getValue', value);
            case 'at.enfilo.def.datatype.DEFInteger'
                value = javaMethod('getValue', value);
            case 'at.enfilo.def.datatype.DEFLong'
                value = javaMethod('getValue', value);
            case 'at.enfilo.def.datatype.DEFString'
                value = char(javaMethod('getValue', value));
            
            % Vectors
            case 'at.enfilo.def.datatype.DEFBooleanVector'
            case 'at.enfilo.def.datatype.DEFDoubleVector'
            case 'at.enfilo.def.datatype.DEFIntegerVector'
            case 'at.enfilo.def.datatype.DEFLongVector'
            case 'at.enfilo.def.datatype.DEFSpringVector'
            
            % Matrices
            case 'at.enfilo.def.datatype.DEFBooleanMatrix'
            case 'at.enfilo.def.datatype.DEFDoubleMatrix'
            case 'at.enfilo.def.datatype.DEFIntegerMatrix'
            case 'at.enfilo.def.datatype.DEFLongMatrix'
            case 'at.enfilo.def.datatype.DEFStringMatrix'
        end
        
    else
        
    end
end


function vector = mapArrayListToVector(arrayList)
    size = javaMethod('size', arrayList);
    vector = 
end