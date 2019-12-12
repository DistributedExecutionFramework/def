package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
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
public class ProgramMapperTest extends MapperTest<Program, ProgramDTO> {

    private static final Object UNSUPPORTED_OBJ;
    private static final Class<?> UNSUPPORTED_CLASS;

    private static final Program PROGRAM;
    private static final ProgramDTO PROGRAM_DTO;

    static  {
        UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
        UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

        String pId = UUID.randomUUID().toString();
        ExecutionState executionState = ExecutionState.SCHEDULED;
        Instant now = Instant.now();
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("TestUser");

        PROGRAM = new Program();
        PROGRAM.setId(pId);
        PROGRAM.setState(executionState);
        PROGRAM.setCreateTime(now);
        PROGRAM.setFinishTime(now);
        PROGRAM.setMasterLibraryRoutine(true);
        PROGRAM.setOwner(user);

        PROGRAM_DTO = new ProgramDTO();
        PROGRAM_DTO.setId(pId);
        PROGRAM_DTO.setState(executionState);
        PROGRAM_DTO.setCreateTime(now.toEpochMilli());
        PROGRAM_DTO.setFinishTime(now.toEpochMilli());
        PROGRAM_DTO.setMasterLibraryRoutine(true);
    }

    public ProgramMapperTest() {
        super(PROGRAM, PROGRAM_DTO, Program.class, ProgramDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
    }

    @Test
    public void mapEntityToDTO()
    throws UnsupportedMappingException {

        ProgramDTO programDTO = MapManager.map(PROGRAM, ProgramDTO.class);

        assertNotNull(programDTO);

        assertEquals(programDTO.getId(), PROGRAM.getId());
        assertEquals(programDTO.getState(), PROGRAM.getState());
        assertEquals(programDTO.getCreateTime(), PROGRAM.getCreateTime().toEpochMilli());
        assertEquals(programDTO.getFinishTime(), PROGRAM.getFinishTime().toEpochMilli());
    }

    @Test
    public void mapDTOToEntity()
    throws UnsupportedMappingException {

        Program program = MapManager.map(PROGRAM_DTO, Program.class);

        assertNotNull(program);

        assertEquals(program.getId(), PROGRAM_DTO.getId());
        assertEquals(program.getState(), PROGRAM_DTO.getState());
        assertEquals(program.getCreateTime().toEpochMilli(), PROGRAM_DTO.getCreateTime());
        assertEquals(program.getFinishTime().toEpochMilli(), PROGRAM_DTO.getFinishTime());
    }
}