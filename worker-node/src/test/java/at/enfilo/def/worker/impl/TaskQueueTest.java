package at.enfilo.def.worker.impl;

import at.enfilo.def.node.api.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.queue.TaskQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class TaskQueueTest {
	private TaskQueue taskQueue;

	@Before
	public void setUp() throws Exception {
		taskQueue = new TaskQueue("test");
	}

	@Test
	public void queueTask() throws Exception {
		assertEquals(0, taskQueue.size());

		TaskDTO t1 = new TaskDTO();
		t1.setId("t1");
		taskQueue.queue(t1);
		assertEquals(1, taskQueue.size());

		TaskDTO t2 = new TaskDTO();
		t2.setId("t2");
		taskQueue.queue(t2);
		assertEquals(2, taskQueue.size());

		List<String> taskIds = taskQueue.getQueuedTasks();
		assertEquals("t1", taskIds.get(0));
		assertEquals("t2", taskIds.get(1));
	}


	@Test
	public void enqueueTask() throws Exception {
		taskQueue.release();
		assertTrue(taskQueue.isReleased());

		Random rnd = new Random();
		int tasks = rnd.nextInt(100);
		for (int i = 0; i < tasks; i++) {
			TaskDTO task = new TaskDTO();
			task.setId(Integer.toString(i));
			taskQueue.queue(task);
		}
		assertEquals(tasks, taskQueue.size());

		for (int i = 0; i < tasks; i++) {
			TaskDTO t = taskQueue.enqueue();
			assertEquals(Integer.toString(i), t.getId());
		}
		assertEquals(0, taskQueue.size());
	}


	@Test
	public void releasePauseQueue() throws Exception {
		TaskDTO t = new TaskDTO();
		t.setId(UUID.randomUUID().toString());
		taskQueue.queue(t);
		assertFalse(taskQueue.isReleased());
		try {
			taskQueue.enqueue();
			fail();
		} catch (QueueNotReleasedException e) {

			taskQueue.release();
			taskQueue.enqueue();
		}

		t = new TaskDTO();
		t.setId(UUID.randomUUID().toString());
		taskQueue.queue(t);
		assertTrue(taskQueue.isReleased());
		taskQueue.pause();
		try {
			taskQueue.enqueue();
			fail();
		} catch (QueueNotReleasedException e) {

			taskQueue.release();
			taskQueue.enqueue();
		}
	}

	@Test
	public void removeTask() throws Exception {
		TaskDTO t1 = new TaskDTO();
		t1.setId("t1");
		taskQueue.queue(t1);
		TaskDTO t2 = new TaskDTO();
		t2.setId("t2");
		taskQueue.queue(t2);
		TaskDTO t3 = new TaskDTO();
		t3.setId("t3");
		taskQueue.queue(t3);

		assertEquals(3, taskQueue.size());

		TaskDTO tRemoved = taskQueue.remove("t2");
		assertEquals(t2, tRemoved);
		assertEquals(2, taskQueue.size());
	}

	@After
	public void tearDown() throws Exception {
		taskQueue = null;
	}
}
