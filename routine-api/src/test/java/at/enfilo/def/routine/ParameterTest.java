package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ParameterTest {
	private Parameter<DEFInteger> parameter;
	private int value;

	@Before
	public void setUp() throws Exception {
		Random rnd = new Random();
		value = rnd.nextInt();

		parameter = new Parameter<>(DEFInteger.class, new DEFInteger(value));
	}

	@Test
	public void getTypeAndValue() throws Exception {
		assertEquals(DEFInteger.class, parameter.getType());
		assertEquals(value, parameter.getValue().getValue());
	}

	@Test
	public void getGenericValue() throws Exception {
		assertEquals(value, parameter.getValue(DEFInteger.class).getValue());
	}

	@Test(expected = WrongTypeException.class)
	public void getWrongGenericValue() throws Exception {
		parameter.getValue(DEFDouble.class);
	}
}
