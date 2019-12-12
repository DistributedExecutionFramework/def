package at.enfilo.def.node.routine.exec;

class RoutineProcess {
	private final Process process;
	private final ProcessBuilder processBuilder;
	private final SequenceStep sequenceStep;

	public RoutineProcess(Process process, ProcessBuilder processBuilder, SequenceStep sequenceStep) {
		this.process = process;
		this.processBuilder = processBuilder;
		this.sequenceStep = sequenceStep;
	}

	public Process getProcess() {
		return process;
	}

	public ProcessBuilder getProcessBuilder() {
		return processBuilder;
	}

	public SequenceStep getSequenceStep() {
		return sequenceStep;
	}
}
