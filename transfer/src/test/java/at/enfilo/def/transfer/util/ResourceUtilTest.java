package at.enfilo.def.transfer.util;

import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class ResourceUtilTest {
	@Test
	public void isSharedResource() {
		ResourceDTO r1 = new ResourceDTO();
		r1.setId(UUID.randomUUID().toString());
		assertTrue(ResourceUtil.isSharedResource(r1));

		ResourceDTO r2 = new ResourceDTO();
		r2.setDataTypeId(UUID.randomUUID().toString());
		r2.setData(new byte[]{0, 1, 2, 3, 4});
		assertFalse(ResourceUtil.isSharedResource(r2));

		ResourceDTO r3 = new ResourceDTO();
		r3.setId(UUID.randomUUID().toString());
		r3.setDataTypeId(UUID.randomUUID().toString());
		r3.setData(new byte[]{0, 1, 2, 3, 4});
		assertFalse(ResourceUtil.isSharedResource(r3));
	}
}
