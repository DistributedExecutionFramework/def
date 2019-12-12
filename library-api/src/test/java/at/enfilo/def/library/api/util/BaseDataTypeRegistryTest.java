package at.enfilo.def.library.api.util;

import at.enfilo.def.transfer.dto.DataTypeDTO;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class BaseDataTypeRegistryTest {
	@Test
	public void getByNameAndId() throws Exception {
		String[] names = new String[] {"DEFString", "DEFIntegerVector", "DEFLongMatrix"};

		for (String name : names) {
			DataTypeDTO dataType = BaseDataTypeRegistry.getInstance().getDataTypeByName(name);
			assertNotNull(dataType);
			DataTypeDTO dataType2 = BaseDataTypeRegistry.getInstance().getById(dataType.getId());
			assertEquals(dataType, dataType2);
		}
	}

	@Test
	public void getAll() throws Exception {
		Collection<DataTypeDTO> dataTypes = BaseDataTypeRegistry.getInstance().getAll();
		assertFalse(dataTypes.isEmpty());

		for (DataTypeDTO dataType : dataTypes) {
			assertNotNull(dataType.getId());
			assertNotNull(dataType.getName());
			assertNotNull(dataType.getSchema());

			assertFalse(dataType.getId().isEmpty());
			assertFalse(dataType.getName().isEmpty());
			assertFalse(dataType.getSchema().isEmpty());
		}
	}
}
