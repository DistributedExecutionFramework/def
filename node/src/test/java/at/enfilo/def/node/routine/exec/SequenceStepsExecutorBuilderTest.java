package at.enfilo.def.node.routine.exec;

import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineType;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class SequenceStepsExecutorBuilderTest {
	private SequenceStepsBuilder sequenceBuilder;
	private String objectiveRoutineId;
	private String mapRoutineId;
	private String partitionRoutineId;
	private String storeRoutineId;
	private TaskDTO task;

	@Before
	public void setUp() throws Exception {
		// Create demo routines
		objectiveRoutineId = UUID.randomUUID().toString();
		mapRoutineId = UUID.randomUUID().toString();
		partitionRoutineId = UUID.randomUUID().toString();
		storeRoutineId = UUID.randomUUID().toString();

		// Create task
		task = new TaskDTO(
				UUID.randomUUID().toString(),
				"jid",
				"pid",
				ExecutionState.SCHEDULED,
				0,
				0,
				0,
				objectiveRoutineId,
				mapRoutineId,
				null,
				null,
				null,
				0
		);

		sequenceBuilder = new SequenceStepsBuilder(task.getId(), NodeConfiguration.getDefault());
	}

	@Test
	public void singleSequence() throws Exception {
		List<SequenceStep> steps = sequenceBuilder.getSequence();
		assertEquals(0, steps.size());
		sequenceBuilder.appendStep(task.getObjectiveRoutineId(), RoutineType.OBJECTIVE);

		steps = sequenceBuilder.getSequence();
		assertEquals(1, steps.size());
		assertEquals(objectiveRoutineId, steps.get(0).getRoutineId());

		NodeConfiguration configuration = NodeConfiguration.getDefault();

		RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				new LibraryServiceClientFactory().createClient(configuration.getLibraryEndpoint()),
				configuration
		);
		SequenceStepsExecutor sequenceStepsExecutor = sequenceBuilder.build(task, routineProcessBuilderFactory);

		// Proof Sequence Step
		int pipeNameCounter = 0;
		SequenceStep step = steps.get(0);
		assertNotNull(step.getCtrlPipe());
		assertEquals(String.format(SequenceStepsBuilder.CTRL_PIPE_FORMAT, pipeNameCounter), step.getCtrlPipe().getName());
		assertNotNull(step.getInPipe());
		assertEquals(String.format(SequenceStepsBuilder.IN_PIPE_FORMAT, pipeNameCounter), step.getInPipe().getName());
		assertTrue(step.getInPipe().isFullyConnected());
		assertFalse(step.hasOutPipe());
		assertNull(step.getOutPipe());

		// Proof Communicator
		RoutinesCommunicator communicator = sequenceStepsExecutor.getCommunicator();
		assertNotNull(communicator);
	}

	@Test
	public void fullSequence() throws Exception {
		List<SequenceStep> steps = sequenceBuilder
										.appendStep(objectiveRoutineId, RoutineType.OBJECTIVE)
										.appendStep(mapRoutineId, RoutineType.MAP)
										.appendStep(storeRoutineId, RoutineType.STORE)
										.getSequence();

		int nrSteps = 3;

		// Check right order and routines
		assertEquals(nrSteps, steps.size());
		assertEquals(objectiveRoutineId, steps.get(0).getRoutineId());
		assertEquals(mapRoutineId, steps.get(1).getRoutineId());
		assertEquals(storeRoutineId, steps.get(2).getRoutineId());

		// Check if pipes are assigned correct between the steps
		for (int i = 0; i < nrSteps; i++) {
			SequenceStep step = steps.get(i);
			// input pipe must be set
			assertNotNull(step.getInPipe());
			// check name of pipe
			assertEquals(String.format(SequenceStepsBuilder.IN_PIPE_FORMAT, i), step.getInPipe().getName());
			assertEquals(String.format(SequenceStepsBuilder.CTRL_PIPE_FORMAT, i), step.getCtrlPipe().getName());
			// out-pipe from previous step must be in-pipe from current step
			if (i > 0) {
				assertSame(steps.get(i - 1).getOutPipe(), step.getInPipe());
				assertTrue(step.getInPipe().isFullyConnected());
			}
			// except last step, every pipe needs a out pipe
			if (i < nrSteps - 1) {
				assertNotNull(step.getOutPipe());
			}
			i++;
		}
	}

}
