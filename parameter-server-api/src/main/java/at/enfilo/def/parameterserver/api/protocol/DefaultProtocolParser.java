package at.enfilo.def.parameterserver.api.protocol;

import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DefaultProtocolParser implements IParameterProtocolParser {

    @Override
    public ParameterProtocol getAssociation() {
        return ParameterProtocol.DEFAULT;
    }

    @Override
    public Object decode(ResourceDTO parameter) throws ProtocolParseException {
        Object result = null;
        byte[] data = parameter.getData();
        if (parameter.getDataTypeId() == null) {
            throw new ProtocolParseException(new IllegalArgumentException("Could not parse data. Type id is null"));
        }
        if (data == null) {
            return null;
        }

        switch (parameter.getDataTypeId().toLowerCase()) {
            //TODO
        }
        return result;
    }

    @Override
    public ResourceDTO encode(Object data, String typeId) throws ProtocolParseException {
        //TODO
        return null;
    }

    private static <T extends TBase> T extractValueFromResource(ResourceDTO resource, Class<T> dataType) throws ProtocolParseException {
        try {
            T value = dataType.newInstance();
            new TDeserializer().deserialize(value, resource.getData());
            return value;
        } catch (IllegalAccessException | InstantiationException | TException e) {
            throw new ProtocolParseException(e);
        }
    }

    private static <T extends Serializable> byte[] toBytes(T obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        }
    }
}
