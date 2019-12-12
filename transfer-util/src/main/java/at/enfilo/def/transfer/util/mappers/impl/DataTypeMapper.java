package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.transfer.dto.DataTypeDTO;

public class DataTypeMapper extends AbstractMapper<DataTypeDTO, DataType> {

	public DataTypeMapper() {
		super(DataTypeDTO.class, DataType.class);
	}

	@Override
	public DataType map(DataTypeDTO source, DataType destination)
	throws IllegalArgumentException, IllegalStateException {

		DataType dest = destination;
		if (dest == null) {
			dest = new DataType();
		}

		mapAttributes(source::getId, dest::setId);
		mapAttributes(source::getName, dest::setName);
		mapAttributes(source::getSchema, dest::setSchema);

		return dest;
	}
}
