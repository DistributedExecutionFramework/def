package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.MapperTest;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class DataTypeMapperTest extends MapperTest<DataType, DataTypeDTO> {


	private static final Object UNSUPPORTED_OBJ;
	private static final Class<?> UNSUPPORTED_CLASS;

	static final DataType DATA_TYPE;
	static final DataTypeDTO DATA_TYPE_DTO;

	static  {
		UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
		UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

		String id = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		String schema = UUID.randomUUID().toString();

		DATA_TYPE = new DataType();
		DATA_TYPE.setId(id);
		DATA_TYPE.setName(name);
		DATA_TYPE.setSchema(schema);

		DATA_TYPE_DTO = new DataTypeDTO();
		DATA_TYPE_DTO.setId(id);
		DATA_TYPE_DTO.setName(name);
		DATA_TYPE_DTO.setSchema(schema);
	}

	public DataTypeMapperTest() {
		super(DATA_TYPE, DATA_TYPE_DTO, DataType.class, DataTypeDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
	}

	@Test
	public void mapEntityToDTO() throws UnsupportedMappingException {
		DataTypeDTO dataTypeDTO = MapManager.map(DATA_TYPE, DataTypeDTO.class);

		assertNotNull(dataTypeDTO);
		assertEquals(dataTypeDTO.getId(), DATA_TYPE_DTO.getId());
		assertEquals(dataTypeDTO.getName(), DATA_TYPE_DTO.getName());
		assertEquals(dataTypeDTO.getSchema(), DATA_TYPE_DTO.getSchema());
	}


	@Test
	public void mapDTOToEntity() throws UnsupportedMappingException {
		DataType dataType = MapManager.map(DATA_TYPE_DTO, DataType.class);

		assertNotNull(dataType);
		assertEquals(dataType.getId(), DATA_TYPE.getId());
		assertEquals(dataType.getName(), DATA_TYPE.getName());
		assertEquals(dataType.getSchema(), DATA_TYPE.getSchema());
	}
}
