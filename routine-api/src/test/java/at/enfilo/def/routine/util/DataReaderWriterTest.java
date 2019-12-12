package at.enfilo.def.routine.util;

import at.enfilo.def.datatype.DEFBoolean;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public abstract class DataReaderWriterTest {

	private static final int LOOPS = 100;

	private DataReader dataReader;
	private DataWriter dataWriter;

	@Before
	public void setUp() throws Exception {
		init();
		dataReader = getDataReader();
		dataWriter = getDataWriter();
	}

	protected abstract DataReader getDataReader();

	protected abstract DataWriter getDataWriter();

	protected abstract void init() throws Exception;


	@Test
	public void writeAndReadScalars() throws Exception {
		Random rnd = new Random();
		for (int i = 0; i < LOOPS; i++) {
			// Integers
			int intValue = rnd.nextInt();
			dataWriter.store(intValue);
			assertEquals(intValue, dataReader.readInt());

			// Strings
			String strValue = UUID.randomUUID().toString();
			dataWriter.store(strValue);
			assertEquals(strValue, dataReader.readString());
		}
	}


	@Test
	public void writeAndReadObjects() throws Exception {
		Random rnd = new Random();
		TSerializer serializer = new TSerializer();

		for (int i = 0; i < LOOPS; i++) {
			// Write and read DEFBoolean structs
			DEFBoolean bValue = new DEFBoolean(rnd.nextBoolean());
			dataWriter.store(bValue);
			assertEquals(bValue, dataReader.read(new DEFBoolean()));

			// Write and read DEFDouble structs
			DEFDouble dValue = new DEFDouble(rnd.nextDouble());
			dataWriter.store(dValue);
			assertEquals(dValue, dataReader.read(new DEFDouble()));

			// Write and read a more complex object: TaskDTO which is holds older structs
			TaskDTO tValue =  new TaskDTO();
			tValue.setId(UUID.randomUUID().toString());
			tValue.setObjectiveRoutineId(UUID.randomUUID().toString());
			tValue.setCreateTime(rnd.nextLong());
			tValue.setStartTime(rnd.nextLong());
			tValue.setFinishTime(rnd.nextLong());
			Map<String, ResourceDTO> params = new HashMap<>();
			params.put("bValue", new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
			params.get("bValue").setData(serializer.serialize(bValue));
			params.put("dValue", new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
			params.get("dValue").setData(serializer.serialize(dValue));
			tValue.setInParameters(params);
			dataWriter.store(tValue);
			assertEquals(tValue, dataReader.read(new TaskDTO()));
		}

	}


	@Test
	public void writeBytesReadObjects() throws Exception {
		Random rnd = new Random();
		TSerializer serializer = new TSerializer();

		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setObjectiveRoutineId(UUID.randomUUID().toString());
		task.setCreateTime(rnd.nextLong());
		task.setStartTime(rnd.nextLong());
		task.setFinishTime(rnd.nextLong());
		List<ResourceDTO> params = new LinkedList<>();
		params.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		params.get(0).setData(serializer.serialize(new DEFInteger(rnd.nextInt())));
		params.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		params.get(1).setData(serializer.serialize(new DEFDouble(rnd.nextDouble())));
		dataWriter.store(serializer.serialize(task));
		assertEquals(task, dataReader.read(new TaskDTO()));
	}


	@Test
	public void writeObjectsReadBytes() throws Exception {
		Random rnd = new Random();
		TSerializer serializer = new TSerializer();
		TDeserializer deserializer = new TDeserializer();

		// Create complex object to store
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setObjectiveRoutineId(UUID.randomUUID().toString());
		task.setCreateTime(rnd.nextLong());
		task.setStartTime(rnd.nextLong());
		task.setFinishTime(rnd.nextLong());
		List<ResourceDTO> params = new LinkedList<>();
		params.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		params.get(0).setData(serializer.serialize(new DEFInteger(rnd.nextInt())));
		params.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		params.get(1).setData(serializer.serialize(new DEFDouble(rnd.nextDouble())));
		dataWriter.store(task);

		// Receive bytes and construct an object
		int len = serializer.serialize(task).length;
		byte[] buf = dataReader.readBytes(len);
		TaskDTO received = new TaskDTO();
		deserializer.deserialize(received, buf);
		Assert.assertEquals(task, received);
	}

	@After
	public void tearDown() throws Exception {
		shutdown();
	}

	protected abstract void shutdown() throws Exception;
}
