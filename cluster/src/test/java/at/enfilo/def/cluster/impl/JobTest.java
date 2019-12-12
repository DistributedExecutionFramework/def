package at.enfilo.def.cluster.impl;

import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JobTest {
	private Job job;

	@Before
	public void setUp() throws Exception {
		Program program = new Program("user1");
		job = new Job(program);
	}

	@Test
	public void allTasksFinished() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		String t4Id = UUID.randomUUID().toString();
		TaskDTO task1 = new TaskDTO();
		task1.setId(t1Id);
		TaskDTO task2 = new TaskDTO();
		task2.setId(t2Id);
		TaskDTO task3 = new TaskDTO();
		task3.setId(t3Id);
		TaskDTO task4 = new TaskDTO();
		task4.setId(t4Id);

		job.addTask(task1);
		job.addTask(task2);
		job.addTask(task3);
		job.addTask(task4);
		job.setComplete(true);

		assertFalse(job.allTasksFinished());

		// Set 2 task to success and 2 to failed
		job.notifyTaskChangedState(t1Id, ExecutionState.SUCCESS);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t2Id, ExecutionState.SUCCESS);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t3Id, ExecutionState.FAILED);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t4Id, ExecutionState.FAILED);

		assertTrue(job.allTasksFinished());
		assertFalse(job.allTasksSuccessful());
	}

	@Test
	public void allTasksSuccessful() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();

		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);

		job.addTask(t1);
		job.addTask(t2);
		job.addTask(t3);
		job.setComplete(true);

		assertFalse(job.allTasksSuccessful());
		assertFalse(job.allTasksFinished());

		// Set all tasks of success
		job.notifyTaskChangedState(t1Id, ExecutionState.SUCCESS);
		assertFalse(job.allTasksSuccessful());
		job.notifyTaskChangedState(t2Id, ExecutionState.SUCCESS);
		assertFalse(job.allTasksSuccessful());
		job.notifyTaskChangedState(t3Id, ExecutionState.SUCCESS);
		assertTrue(job.allTasksSuccessful());
		assertTrue(job.allTasksFinished());
	}

	@Test
	public void getNumberOfTasks() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();

		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);

		assertEquals(0, job.getNumberOfTasks());

		job.addTask(t1);
		job.addTask(t2);
		job.addTask(t3);
		assertEquals(3, job.getNumberOfTasks());
		job.addTask(t3); // Add the same task one more
		assertEquals(3, job.getNumberOfTasks());

		job.notifyTaskChangedState(t1Id, ExecutionState.RUN);
		assertEquals(3, job.getNumberOfTasks());
		job.notifyTaskChangedState(t2Id, ExecutionState.FAILED);
		assertEquals(3, job.getNumberOfTasks());
		job.notifyTaskChangedState(t2Id, ExecutionState.FAILED);
	}

	@Test
	public void getTasksByState() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);
		job.addTask(t1);
		job.addTask(t2);
		job.addTask(t3);

		assertEquals(3, job.getAllTasks().size());
		assertEquals(3, job.getScheduledTasks().size());

		job.notifyTaskChangedState(t1Id, ExecutionState.RUN);
		assertEquals(2, job.getScheduledTasks().size());
		assertEquals(1, job.getRunningTasks().size());
		assertTrue(job.getRunningTasks().contains(t1Id));
		assertFalse(job.getScheduledTasks().contains(t1Id));

		job.notifyTaskChangedState(t1Id, ExecutionState.SUCCESS);
		assertEquals(2, job.getScheduledTasks().size());
		assertEquals(0, job.getRunningTasks().size());
		assertEquals(1, job.getSuccessfulTasks().size());
		assertTrue(job.getSuccessfulTasks().contains(t1Id));
	}

	@Test
	public void concurrencyStateChange() throws JobCompletedException, InterruptedException {
		List<List<String>> sets = new LinkedList<>();
		int threads = 4;
		int tasksPerThread = 10000;
		for (int j = 0; j < threads; j++) {
			List<String> set = new LinkedList<>();
			sets.add(set);
			for (int i = 0; i < tasksPerThread; i++) {
				String tId = UUID.randomUUID().toString();
				TaskDTO task = new TaskDTO();
				task.setId(tId);
				job.addTask(task);
				set.add(tId);
			}
		}

		ExecutorService es = Executors.newFixedThreadPool(4);
		for (final List<String> set : sets) {
			es.submit(() -> {
				for (ExecutionState state : new ExecutionState[]{ExecutionState.RUN, ExecutionState.SUCCESS}) {
					for (String tId : set) {
						job.notifyTaskChangedState(tId, state);
					}
				}
			});
		}
		es.shutdown();
		es.awaitTermination(60, TimeUnit.SECONDS);

		assertEquals(0, job.getNumberOfScheduledTasks());
		assertEquals(0, job.getNumberOfRunningTasks());
		assertEquals(0, job.getNumberOfFailedTasks());
		assertEquals(threads * tasksPerThread, job.getNumberOfSuccessfulTasks());
	}

}
