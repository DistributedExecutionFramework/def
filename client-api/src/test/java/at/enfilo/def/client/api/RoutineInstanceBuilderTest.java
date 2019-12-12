package at.enfilo.def.client.api;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.transfer.dto.RoutineInstanceDTO;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoutineInstanceBuilderTest {
	@Test
	public void build() throws TException {
		String rId = UUID.randomUUID().toString();
		Random rnd = new Random();
		TDeserializer deserializer = new TDeserializer();

		String param1Name = UUID.randomUUID().toString();
		DEFDouble param1Value = new DEFDouble(rnd.nextDouble());
		String param2Name = UUID.randomUUID().toString();
		DEFInteger param2Value = new DEFInteger(rnd.nextInt());

		RoutineInstanceDTO instance = new RoutineInstanceBuilder(rId)
				.addParameter(param1Name, param1Value)
				.addParameter(param2Name, param2Value)
				.build();

		assertEquals(rId, instance.getRoutineId());
		assertEquals(2, instance.getInParametersSize());
		assertTrue(instance.getInParameters().containsKey(param1Name));
		assertTrue(instance.getInParameters().containsKey(param2Name));
		DEFDouble param1Decoded = new DEFDouble();
		DEFInteger param2Decoded = new DEFInteger();
		deserializer.deserialize(param1Decoded, instance.getInParameters().get(param1Name).data.array());
		deserializer.deserialize(param2Decoded, instance.getInParameters().get(param2Name).data.array());
		assertEquals(param1Value, param1Decoded);
		assertEquals(param2Value, param2Decoded);
	}
}
