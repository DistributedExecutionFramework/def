package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.transfer.dto.FormalParameterDTO;
import at.enfilo.def.transfer.util.MapManager;


public class FormalParameterMapper extends AbstractMapper<FormalParameterDTO, FormalParameter> {

    public FormalParameterMapper() {
        super(FormalParameterDTO.class, FormalParameter.class);
    }

    @Override
    public FormalParameter map(FormalParameterDTO source, FormalParameter destination)
    throws IllegalArgumentException, IllegalStateException {

        FormalParameter dest = destination;
    	if (dest == null) {
            dest = new FormalParameter();
        }
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getDescription, dest::setDescription);
            mapAttributes(
                source::getDataType,
                dest::setDataType,
                dataTypeDTO -> MapManager.map(dataTypeDTO, DataType.class)
            );

            return dest;
        }
        return null;
    }
}
