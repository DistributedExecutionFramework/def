package at.enfilo.def.dto.cache.impl;

import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DTODiskPersistenceDriverTest {
	private DTODiskPersistenceDriver<TaskDTO> driver;

	@Before
	public void setUp() throws Exception {
		File path = new File("/tmp/def/task-cache");
		path.delete();

		driver = new DTODiskPersistenceDriver<>();
		driver.init("file:/tmp/def/task-cache", "unit-test");
	}

	@Test
	public void writeAndRead() throws Exception {
		TaskDTO origin = createTask();
		driver.write(origin.getId(), origin);
		TaskDTO task = driver.read(origin.getId(), new TaskDTO());
		assertEquals(origin, task);
	}

	@Test
	public void update() throws Exception {
		TaskDTO origin = createTask();
		driver.write(origin.getId(), origin);

		origin.setState(ExecutionState.SUCCESS);
		origin.setFinishTime(System.currentTimeMillis());
		driver.write(origin.getId(), origin);

		TaskDTO task = driver.read(origin.getId(), new TaskDTO());
		assertEquals(origin, task);
	}

	@Test(expected = IOException.class)
	public void readUnknownTask() throws Exception {
		driver.read(UUID.randomUUID().toString(), new TaskDTO());
	}

	@Test(expected = IOException.class)
	public void delete() throws Exception {
		TaskDTO task = createTask();
		driver.write(task.getId(), task);
		driver.remove(task.getId());
		driver.read(task.getId(), new TaskDTO());
	}

	private TaskDTO createTask() {
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setJobId(UUID.randomUUID().toString());
		task.setProgramId(UUID.randomUUID().toString());
		task.setCreateTime(System.currentTimeMillis());
		task.setFinishTime(System.currentTimeMillis());
		task.setState(ExecutionState.RUN);
		task.setObjectiveRoutineId(UUID.randomUUID().toString());
		task.setMapRoutineId(UUID.randomUUID().toString());
		task.putToInParameters(UUID.randomUUID().toString(), new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		task.putToInParameters(UUID.randomUUID().toString(), new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		task.addToOutParameters(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		return task;
	}
}
