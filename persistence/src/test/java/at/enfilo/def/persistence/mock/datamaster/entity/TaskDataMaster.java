package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.domain.entity.Task_;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineType;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by mase on 02.09.2016.
 */
public class TaskDataMaster extends DataMaster<Task, String> {

	private static Task DUMMY_TASK_1;
	private static Task DUMMY_TASK_2;

	static {
		//try {
			Routine objectiveRoutine = new Routine();
			objectiveRoutine.setId(UUID.randomUUID().toString());
			objectiveRoutine.setType(RoutineType.OBJECTIVE);
			Routine mapRoutine = new Routine();
			mapRoutine.setType(RoutineType.MAP);
			mapRoutine.setId(UUID.randomUUID().toString());
			DUMMY_TASK_1 = new Task();
			DUMMY_TASK_1.setState(ExecutionState.SCHEDULED);
			DUMMY_TASK_1.setCreateTime(Instant.now());
			DUMMY_TASK_1.setStartTime(Instant.now());
			DUMMY_TASK_1.setFinishTime(Instant.now());
			//DUMMY_TASK_1.setObjectiveRoutine(objectiveRoutine);
			//DUMMY_TASK_1.setMapRoutine(mapRoutine);

			DUMMY_TASK_2 = new Task();
			DUMMY_TASK_2.setState(ExecutionState.RUN);
			DUMMY_TASK_2.setCreateTime(Instant.now());
			DUMMY_TASK_2.setStartTime(Instant.now());
			DUMMY_TASK_2.setFinishTime(Instant.now());
			//DUMMY_TASK_2.setObjectiveRoutine(objectiveRoutine);
			//DUMMY_TASK_2.setMapRoutine(mapRoutine);
		//} catch (WrongRoutineTypeException e) {
		//	e.printStackTrace();
		//	fail();
		//}
	}

    public TaskDataMaster() {
        super(Task.class, String.class, Task_.id);

		registerDummyEntity(DUMMY_TASK_1);
		registerDummyEntity(DUMMY_TASK_2);
	}

	@Override
	public Task updateEntity(Task initialEntity) {
		initialEntity.setState(ExecutionState.SUCCESS);
		initialEntity.setCreateTime(Instant.now());
		initialEntity.setStartTime(Instant.now());
		initialEntity.setFinishTime(Instant.now());

		return initialEntity;
	}

	@Override
	public Task getDummyEntityForSearch() {
		return DUMMY_TASK_1;
	}

	@Override
	public Task getDummyEntityForSave() {
		Task dummyTask = new Task();
		dummyTask.setState(ExecutionState.FAILED);
		dummyTask.setCreateTime(Instant.now());
		dummyTask.setStartTime(Instant.now());
		dummyTask.setFinishTime(Instant.now());

		return dummyTask;
	}

	@Override
	public Task getDummyEntityForUpdate() {
		return DUMMY_TASK_2;
	}

	@Override
	public Task getDummyEntityForDelete() {
		return DUMMY_TASK_1;
	}
}
