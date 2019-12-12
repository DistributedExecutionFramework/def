package at.enfilo.def.node.routine.exec;

import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.*;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class SequenceStepsBuilder {
	public static final String IN_PIPE_FORMAT = "in_%d";
	public static final String CTRL_PIPE_FORMAT = "ctrl_%d";

	private final LinkedList<SequenceStep> steps = new LinkedList<>();
	private final NodeConfiguration configuration;
	private final String pipeDir;
	private int inPipeNameCounter = 0;
	private int ctrlPipeNameCounter = 0;

	public SequenceStepsBuilder(String id, NodeConfiguration configuration) {
		this.configuration = configuration;
		this.pipeDir = Paths.get(configuration.getWorkingDir(), id).toAbsolutePath().toString();
	}

	/**
	 * Append a step to current TaskSequence.
	 *
	 * @param routineId - routine to append
	 * @return
	 */
	public SequenceStepsBuilder appendStep(String routineId, RoutineType type) {
		Pipe inPipe = new Pipe(pipeDir, nextInPipeName());
		Pipe ctrlPipe = new Pipe(pipeDir, nextCtrlPipeName());
		if (!steps.isEmpty()) {
			steps.getLast().setOutPipe(inPipe);
		}
		SequenceStep step = new SequenceStep(routineId, type, inPipe, ctrlPipe);
		steps.add(step);
		return this;
	}

	public List<SequenceStep> getSequence() {
		return steps;
	}

	private String nextInPipeName() {
		return String.format(IN_PIPE_FORMAT, inPipeNameCounter++);
	}

	private String nextCtrlPipeName(){
		return String.format(CTRL_PIPE_FORMAT, ctrlPipeNameCounter++);
	}

	/**
	 * Build and return a {@link SequenceStepsExecutor}.
	 * @return
	 */
	public SequenceStepsExecutor build(TaskDTO task, RoutineProcessBuilderFactory routineProcessBuilderFactory) {
		return new SequenceStepsExecutor(
				task.getId(),
				steps,
				task.getInParameters(),
				false,
				configuration,
				routineProcessBuilderFactory,
				DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId())
		);
	}

	public SequenceStepsExecutor build(ProgramDTO program, RoutineProcessBuilderFactory routineProcessBuilderFactory) {
		return new SequenceStepsExecutor(
				program.getId(),
				steps,
				null,
				false,
				configuration,
				routineProcessBuilderFactory,
				DEFLoggerFactory.createProgramContext(program.getId())
		);
	}

	public SequenceStepsExecutor build(ReduceJobDTO reduceJob, RoutineProcessBuilderFactory routineProcessBuilderFactory) {
		return new SequenceStepsExecutor(
				reduceJob.getJobId(),
				steps,
				null,
				true,
				configuration,
				routineProcessBuilderFactory,
				DEFLoggerFactory.createJobContext(reduceJob.getJob().getProgramId(), reduceJob.getJobId())
		);
	}
}
