package at.enfilo.def.node.util;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.api.Result;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ResultUtilTest {

	@Test
	public void extractDataTypeId() throws TException {
		Random rnd = new Random();
		TSerializer serializer = new TSerializer();

		DEFDouble defDouble = new DEFDouble(rnd.nextDouble());

		Result result = new Result();
		result.setKey(UUID.randomUUID().toString());
		result.setSeq(rnd.nextInt());
		result.setData(serializer.serialize(defDouble));

		String dataTypeId = ResultUtil.extractDataTypeId(result);
		assertEquals(defDouble._id, dataTypeId);

		dataTypeId = ResultUtil.extractDataTypeId(result, new TDeserializer());
		assertEquals(defDouble._id, dataTypeId);
	}
}
