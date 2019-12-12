package at.enfilo.def.manager.webservice.util;

import at.enfilo.def.datatype.*;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

public class DataConverter {

    public String convertResourceData(ResourceDTO resource) throws TException {
        String data;
        TDeserializer deserializer = new TDeserializer();
        switch (resource.getDataTypeId()) {
            case "13557af3-2524-3252-9b65-f288b64d922b":
                DEFBoolean b = new DEFBoolean();
                deserializer.deserialize(b, resource.data.array());
                data = Boolean.toString(b.isValue());
                break;
            case "6389a2fb-eace-310b-b178-9c4d7b1daaa0":
                DEFInteger i = new DEFInteger();
                deserializer.deserialize(i, resource.data.array());
                data = Integer.toString(i.getValue());
                break;
            case "5fb4621b-8de1-39f8-b282-9108cfe2adc0":
                DEFLong l = new DEFLong();
                deserializer.deserialize(l, resource.data.array());
                data = Long.toString(l.getValue());
                break;
            case "6e8d4e97-38f8-31df-887d-8b193c2e50b3":
                DEFDouble d = new DEFDouble();
                deserializer.deserialize(d, resource.data.array());
                data = Double.toString(d.getValue());
                break;
            case "b5f087fc-e8b3-3e2d-9e46-7492c2cb36cf":
                DEFString s = new DEFString();
                deserializer.deserialize(s, resource.data.array());
                data = s.getValue();
                break;
            default:
                data = String.valueOf(resource.data.array().length) + " bytes";
                break;
        }
        return data;
    }
}
