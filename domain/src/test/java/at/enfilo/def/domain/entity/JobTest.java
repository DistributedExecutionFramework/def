package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.exception.JobCompletedException;
import at.enfilo.def.transfer.dto.ExecutionState;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JobTest {
	private Job job;

	@Before
	public void setUp() throws Exception {
		Program program = new Program();
		job = new Job();
		job.setProgram(program);
	}

	@Test
	public void allTasksFinished() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		String t4Id = UUID.randomUUID().toString();

		job.addTask(t1Id);
		job.addTask(t2Id);
		job.addTask(t3Id);
		job.addTask(t4Id);
		job.setComplete(true);

		assertFalse(job.allTasksFinished());

		// Set 2 task to success and 2 to failed
		job.notifyTaskChangedState(t1Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t2Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t3Id, ExecutionState.RUN, ExecutionState.FAILED);
		assertFalse(job.allTasksFinished());
		job.notifyTaskChangedState(t4Id, ExecutionState.RUN, ExecutionState.FAILED);

		assertTrue(job.allTasksFinished());
		assertFalse(job.allTasksSuccessful());
	}

	@Test
	public void allTasksSuccessful() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();

		job.addTask(t1Id);
		job.addTask(t2Id);
		job.addTask(t3Id);
		job.setComplete(true);

		assertFalse(job.allTasksSuccessful());
		assertFalse(job.allTasksFinished());

		// Set all tasks of success
		job.notifyTaskChangedState(t1Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertFalse(job.allTasksSuccessful());
		job.notifyTaskChangedState(t2Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertFalse(job.allTasksSuccessful());
		job.notifyTaskChangedState(t3Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertTrue(job.allTasksSuccessful());
		assertTrue(job.allTasksFinished());
	}

	@Test
	public void getNumberOfTasks() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();

		assertEquals(0, job.getNumberOfTasks());

		job.addTask(t1Id);
		job.addTask(t2Id);
		job.addTask(t3Id);
		assertEquals(3, job.getNumberOfTasks());
		job.addTask(t3Id); // Add the same task one more
		assertEquals(3, job.getNumberOfTasks());

		job.notifyTaskChangedState(t1Id, ExecutionState.SCHEDULED, ExecutionState.RUN);
		assertEquals(3, job.getNumberOfTasks());
		job.notifyTaskChangedState(t2Id, ExecutionState.SCHEDULED, ExecutionState.FAILED);
		assertEquals(3, job.getNumberOfTasks());
		job.notifyTaskChangedState(t2Id, ExecutionState.RUN, ExecutionState.FAILED);
	}

	@Test
	public void getTasksByState() throws JobCompletedException {
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		job.addTask(t1Id);
		job.addTask(t2Id);
		job.addTask(t3Id);

		assertEquals(3, job.getAllTasks().size());
		assertEquals(3, job.getScheduledTasks().size());

		job.notifyTaskChangedState(t1Id, ExecutionState.SCHEDULED, ExecutionState.RUN);
		assertEquals(2, job.getScheduledTasks().size());
		assertEquals(1, job.getRunningTasks().size());
		assertTrue(job.getRunningTasks().contains(t1Id));
		assertFalse(job.getScheduledTasks().contains(t1Id));

		job.notifyTaskChangedState(t1Id, ExecutionState.RUN, ExecutionState.SUCCESS);
		assertEquals(2, job.getScheduledTasks().size());
		assertEquals(0, job.getRunningTasks().size());
		assertEquals(1, job.getSuccessfulTasks().size());
		assertTrue(job.getSuccessfulTasks().contains(t1Id));
	}
}
