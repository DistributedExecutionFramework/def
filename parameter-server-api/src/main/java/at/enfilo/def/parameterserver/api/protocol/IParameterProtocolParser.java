package at.enfilo.def.parameterserver.api.protocol;

import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ResourceDTO;

public interface IParameterProtocolParser {

    ParameterProtocol getAssociation();

    Object decode(ResourceDTO parameter) throws ProtocolParseException;

    ResourceDTO encode(Object data, String typeId) throws ProtocolParseException;
}
