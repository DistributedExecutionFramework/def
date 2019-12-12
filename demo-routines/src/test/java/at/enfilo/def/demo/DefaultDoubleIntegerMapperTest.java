package at.enfilo.def.demo;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class DefaultDoubleIntegerMapperTest {
	@Test
	public void map() throws Exception {
		DefaultDoubleIntegerMapper mapper = new DefaultDoubleIntegerMapper();

		Random rnd = new Random();
		DEFDouble value = new DEFDouble(rnd.nextDouble() * 1000);

		List<ITuple<String, DEFInteger>> mappedValues = mapper.map(value);

		assertEquals(1, mappedValues.size());
		assertEquals(DefaultDoubleIntegerMapper.DEFAULT_KEY, mappedValues.get(0).getKey());
		assertEquals(new Double(value.getValue()).intValue(), mappedValues.get(0).getValue().getValue());
	}
}
