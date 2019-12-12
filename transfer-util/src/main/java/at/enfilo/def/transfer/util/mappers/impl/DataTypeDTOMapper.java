package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.transfer.dto.DataTypeDTO;

public class DataTypeDTOMapper extends AbstractMapper<DataType, DataTypeDTO> {

	public DataTypeDTOMapper() {
		super(DataType.class, DataTypeDTO.class);
	}

	@Override
	public DataTypeDTO map(DataType source, DataTypeDTO destination)
	throws IllegalArgumentException, IllegalStateException {

		DataTypeDTO dest = destination;
		if (dest== null) {
			dest = new DataTypeDTO();
		}

		mapAttributes(source::getId, dest::setId);
		mapAttributes(source::getName, dest::setName);
		mapAttributes(source::getSchema, dest::setSchema);

		return dest;
	}
}
