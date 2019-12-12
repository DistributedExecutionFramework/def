package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.FormalParameterDTO;
import at.enfilo.def.transfer.util.MapManager;


public class FormalParameterDTOMapper extends AbstractMapper<FormalParameter, FormalParameterDTO> {

    public FormalParameterDTOMapper() {
        super(FormalParameter.class, FormalParameterDTO.class);
    }

    @Override
    public FormalParameterDTO map(FormalParameter source, FormalParameterDTO destination)
    throws IllegalArgumentException, IllegalStateException {

    	FormalParameterDTO dest = destination;
        if (dest == null) {
            dest = new FormalParameterDTO();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getDescription, dest::setDescription);
            mapAttributes(
                source::getDataType,
                dest::setDataType,
                dataType -> MapManager.map(dataType, DataTypeDTO.class)
            );

            return dest;
        }
        return null;
    }
}
