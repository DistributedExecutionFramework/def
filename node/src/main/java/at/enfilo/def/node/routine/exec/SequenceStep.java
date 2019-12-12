package at.enfilo.def.node.routine.exec;


import at.enfilo.def.routine.api.IPipeReader;
import at.enfilo.def.routine.api.IPipeWriter;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.RoutineType;

public class SequenceStep implements IPipeWriter, IPipeReader {
	private final Pipe inPipe;
	private final Pipe ctrlPipe;
	private final String routineId;
	private final RoutineType routineType;
	private Pipe outPipe;

	public SequenceStep(String routineId, RoutineType routineType, Pipe inPipe, Pipe ctrlPipe) {
		this.routineId = routineId;
		this.routineType = routineType;
		this.inPipe = inPipe;
		this.inPipe.setReader(this);
		this.ctrlPipe = ctrlPipe;
		this.ctrlPipe.setWriter(this);
	}

	public Pipe getInPipe() {
		return inPipe;
	}

	public Pipe getOutPipe() {
		return outPipe;
	}

	public boolean hasOutPipe() {
		return (outPipe != null);
	}

	public String getRoutineId() {
		return routineId;
	}

	public Pipe getCtrlPipe() {
		return ctrlPipe;
	}

	public RoutineType getRoutineType() {
		return routineType;
	}

	public void setOutPipe(Pipe outPipe) {
		this.outPipe = outPipe;
		this.outPipe.setWriter(this);
	}

	public boolean isOutPipeSet() {
		return outPipe != null;
	}
}

