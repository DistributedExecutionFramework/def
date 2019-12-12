package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.transfer.util.MapperTest;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by mase on 29.08.2016.
 */
public class TaskMapperTest extends MapperTest<Task, TaskDTO> {

    private static final Object UNSUPPORTED_OBJ;
    private static final Class<?> UNSUPPORTED_CLASS;

    private static final Task TASK;
    private static final TaskDTO TASK_DTO;

    static  {
        UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
        UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

        String tId = UUID.randomUUID().toString();
        ExecutionState executionState = ExecutionState.SCHEDULED;
        Instant now = Instant.now();

        TASK = new Task();
        TASK.setId(tId);
        TASK.setState(executionState);
        TASK.setStartTime(now);
        TASK.setCreateTime(now);
        TASK.setFinishTime(now);

        TASK_DTO = new TaskDTO();
        TASK_DTO.setId(tId);
        TASK_DTO.setState(executionState);
        TASK_DTO.setStartTime(now.toEpochMilli());
        TASK_DTO.setCreateTime(now.toEpochMilli());
        TASK_DTO.setFinishTime(now.toEpochMilli());
    }

    public TaskMapperTest() {
        super(TASK, TASK_DTO, Task.class, TaskDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
    }

    @Test
    public void mapEntityToDTO()
    throws UnsupportedMappingException {

        TaskDTO taskDTO = MapManager.map(TASK, TaskDTO.class);

        assertNotNull(taskDTO);

        assertEquals(taskDTO.getId(), TASK.getId());
        assertEquals(taskDTO.getState(), TASK.getState());
        assertEquals(taskDTO.getStartTime(), TASK.getStartTime().toEpochMilli());
        assertEquals(taskDTO.getCreateTime(), TASK.getCreateTime().toEpochMilli());
        assertEquals(taskDTO.getFinishTime(), TASK.getFinishTime().toEpochMilli());
    }

    @Test
    public void mapDTOToEntity()
    throws UnsupportedMappingException {

        Task task = MapManager.map(TASK_DTO, Task.class);

        assertNotNull(task);

        assertEquals(task.getId(), TASK_DTO.getId());
        assertEquals(task.getState(), TASK_DTO.getState());
        assertEquals(task.getStartTime().toEpochMilli(), TASK_DTO.getStartTime());
        assertEquals(task.getCreateTime().toEpochMilli(), TASK_DTO.getCreateTime());
        assertEquals(task.getFinishTime().toEpochMilli(), TASK_DTO.getFinishTime());
    }
}
