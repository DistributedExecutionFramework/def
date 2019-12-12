package at.enfilo.def.worker.queue;

import at.enfilo.def.node.api.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class QueuePriorityWrapperTest {
	private QueuePriorityWrapper queuePriorityWrapper;
	private TaskQueue queue1;
	private TaskQueue queue2;
	private TaskQueue queue3;

	@Before
	public void setUp() throws Exception {
		queue1 = new TaskQueue("1");
		queue1.release();
		queue2 = new TaskQueue("2");
		queue2.release();
		queue3 = new TaskQueue("3");
		queue3.release();

		queuePriorityWrapper = new QueuePriorityWrapper();
	}

	@Test(timeout = 10000)
	public void addAndGetQueue() throws Exception {
		queuePriorityWrapper.addQueue(queue1);
		queuePriorityWrapper.addQueue(queue2);
		queuePriorityWrapper.addQueue(queue3);
		queuePriorityWrapper.addQueue(queue3);

		assertTrue(queuePriorityWrapper.containsQueue(queue1.getQueueId()));
		assertTrue(queuePriorityWrapper.containsQueue(queue2.getQueueId()));
		assertTrue(queuePriorityWrapper.containsQueue(queue3.getQueueId()));
		assertFalse(queuePriorityWrapper.containsQueue(UUID.randomUUID().toString()));

		assertEquals(3, queuePriorityWrapper.getNumberOfQueues());

		assertEquals(queue1, queuePriorityWrapper.getQueue(queue1.getQueueId()));
		assertEquals(queue2, queuePriorityWrapper.getQueue(queue2.getQueueId()));
		assertEquals(queue3, queuePriorityWrapper.getQueue(queue3.getQueueId()));
	}

	@Test(timeout = 10000)
	public void enqueueSingle() throws Exception {
		queuePriorityWrapper.addQueue(queue1);

		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		queuePriorityWrapper.getQueue(queue1.getQueueId()).queue(task);

		TaskDTO enqueuedTask = queuePriorityWrapper.enqueue();
		assertEquals(task, enqueuedTask);
	}

	@Test(timeout = 60000)
	public void enqueueMulti() throws Exception {
		queuePriorityWrapper.addQueue(queue1);
		queuePriorityWrapper.addQueue(queue2);
		queuePriorityWrapper.addQueue(queue3);

		List<TaskDTO> tasks = new LinkedList<>();
		Random rnd = new Random();
		TaskQueue[] taskQueue = new TaskQueue[]{queue1, queue2, queue3};
		for (TaskQueue queue : taskQueue) {
			int samples = rnd.nextInt(1000);
			for (int i = 0; i < samples; i++) {
				TaskDTO task = new TaskDTO();
				task.setId(UUID.randomUUID().toString());
				queue.queue(task);
				tasks.add(task);
			}
		}

		for (TaskDTO task : tasks) {
			assertEquals(task, queuePriorityWrapper.enqueue());
			Thread.sleep(5);
		}
	}

	@Test(timeout = 10000)
	public void enqueueWait() throws Exception {
		queuePriorityWrapper.addQueue(queue1);
		queuePriorityWrapper.addQueue(queue2);
		queuePriorityWrapper.addQueue(queue3);

		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());

		final long[] waitingTime = new long[2];
		final TaskDTO[] enqueuedTask = new TaskDTO[1];
		long sleepMs = 1000;
		long delta = 100;

		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				waitingTime[0] = System.currentTimeMillis();
				enqueuedTask[0] = queuePriorityWrapper.enqueue();
				waitingTime[1] = System.currentTimeMillis();
				} catch (InterruptedException | IOException | QueueNotReleasedException e) {
					fail();
					e.printStackTrace();
				}
			}
		);

		Thread.sleep(sleepMs);
		queue3.queue(task);

		await().atMost(10, TimeUnit.SECONDS).until(() -> task.equals(enqueuedTask[0]));
		assertEquals(waitingTime[1] - waitingTime[0], sleepMs, delta);
	}


	@Test(timeout = 10000)
	public void enqueueThreads() throws Exception {
		queuePriorityWrapper.addQueue(queue1);

		final int nrTasks = 1000;
		final List<TaskDTO> enqueuedTask = Collections.synchronizedList(new LinkedList<>());

		Executors.newSingleThreadExecutor().submit(() -> {
					try {
						while (true) {
							TaskDTO task = queuePriorityWrapper.enqueue();
							enqueuedTask.add(task);
						}
					} catch (InterruptedException | IOException | QueueNotReleasedException e) {
						fail();
						e.printStackTrace();
					}
				}
		);

		List<TaskDTO> tasks = new LinkedList<>();
		for (int i = 0; i < nrTasks; i++) {
			TaskDTO task = new TaskDTO();
			task.setId(UUID.randomUUID().toString());
			queue1.queue(task);
			tasks.add(task);
		}

		await().atMost(10, TimeUnit.SECONDS).until(() -> enqueuedTask.size() == nrTasks);

		assertEquals(tasks, enqueuedTask);
	}

	@Test(timeout = 10000)
	public void delQueue() throws Exception {
		queuePriorityWrapper.addQueue(queue1);
		queuePriorityWrapper.addQueue(queue2);
		queuePriorityWrapper.addQueue(queue3);

		TaskDTO task1 = new TaskDTO();
		task1.setId(UUID.randomUUID().toString());
		TaskDTO task2 = new TaskDTO();
		task2.setId(UUID.randomUUID().toString());
		TaskDTO task3 = new TaskDTO();
		task3.setId(UUID.randomUUID().toString());

		queue1.queue(task1);
		queue2.queue(task2);
		queue3.queue(task3);

		// Delete first two queues, only 3. task should avail
		queuePriorityWrapper.delQueue(queue1.getQueueId());
		queuePriorityWrapper.delQueue(queue2.getQueueId());

		assertEquals(task3, queuePriorityWrapper.enqueue());
	}

	@Test(timeout = 10000)
	public void releaseAndPauseQueue() throws Exception {
		queuePriorityWrapper.addQueue(queue1);
		queuePriorityWrapper.addQueue(queue2);
		queuePriorityWrapper.addQueue(queue3);

		TaskDTO task1 = new TaskDTO();
		task1.setId(UUID.randomUUID().toString());
		TaskDTO task2 = new TaskDTO();
		task2.setId(UUID.randomUUID().toString());
		TaskDTO task3 = new TaskDTO();
		task3.setId(UUID.randomUUID().toString());

		queue1.queue(task1);
		queue2.queue(task2);
		queue3.queue(task3);

		// Pause 1. queue, next task should be task2
		queue1.pause();
		Thread.sleep(100);
		assertEquals(task2, queuePriorityWrapper.enqueue());

		// Release 1. queue, next task should be task1
		queue1.release();
		Thread.sleep(100);
		assertEquals(task1, queuePriorityWrapper.enqueue());
	}
}
