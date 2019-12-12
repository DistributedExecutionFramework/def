package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.transfer.dto.FormalParameterDTO;
import at.enfilo.def.transfer.dto.Language;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import at.enfilo.def.transfer.util.MapperTest;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RoutineMapperTest extends MapperTest<Routine, RoutineDTO> {

    private static final Object UNSUPPORTED_OBJ;
    private static final Class<?> UNSUPPORTED_CLASS;

    private static final Routine ROUTINE;
    private static final RoutineDTO ROUTINE_DTO;

    static  {
        UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
        UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

        String id = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();

        ROUTINE = new Routine();
        ROUTINE.setId(id);
        ROUTINE.setName(name);
        ROUTINE.setDescription(description);
        ROUTINE.setLanguage(Language.JAVA);
        ROUTINE.setType(RoutineType.MAP);
        List<FormalParameter> inParams = new LinkedList<>();
		inParams.add(FormalParameterMapperTest.FORMAL_PARAMETER);
		inParams.add(FormalParameterMapperTest.FORMAL_PARAMETER);
        ROUTINE.setInParameters(inParams);
        ROUTINE.setOutParameter(FormalParameterMapperTest.FORMAL_PARAMETER);

        ROUTINE_DTO = new RoutineDTO();
        ROUTINE_DTO.setId(id);
        ROUTINE_DTO.setName(name);
        ROUTINE_DTO.setDescription(description);
        ROUTINE_DTO.setType(RoutineType.MAP);
		List<FormalParameterDTO> inParamsDTO = new LinkedList<>();
		inParamsDTO.add(FormalParameterMapperTest.FORMAL_PARAMETER_DTO);
		inParamsDTO.add(FormalParameterMapperTest.FORMAL_PARAMETER_DTO);
		ROUTINE_DTO.setInParameters(inParamsDTO);
		ROUTINE_DTO.setOutParameter(FormalParameterMapperTest.FORMAL_PARAMETER_DTO);

    }

    public RoutineMapperTest() {
        super(ROUTINE, ROUTINE_DTO, Routine.class, RoutineDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
    }

    @Test
    public void mapEntityToDTO() throws UnsupportedMappingException {
        RoutineDTO routineDTO = MapManager.map(ROUTINE, RoutineDTO.class);

        assertNotNull(routineDTO);
        assertEquals(routineDTO.getId(), ROUTINE_DTO.getId());
        assertEquals(routineDTO.getName(), ROUTINE_DTO.getName());
        assertEquals(routineDTO.getDescription(), ROUTINE_DTO.getDescription());
		assertEquals(routineDTO.getType(), ROUTINE_DTO.getType());
		assertEquals(routineDTO.getInParameters(), ROUTINE_DTO.getInParameters());
		assertEquals(routineDTO.getOutParameter(), ROUTINE_DTO.getOutParameter());
    }

    @Test
    public void mapDTOToEntity() throws UnsupportedMappingException {
		Routine routine = MapManager.map(ROUTINE_DTO, Routine.class);

		assertNotNull(routine);
		assertEquals(routine.getId(), ROUTINE.getId());
		assertEquals(routine.getName(), ROUTINE.getName());
		assertEquals(routine.getDescription(), ROUTINE.getDescription());
		assertEquals(routine.getType(), ROUTINE.getType());
		assertEquals(routine.getInParameters(), ROUTINE.getInParameters());
		assertEquals(routine.getOutParameter(), ROUTINE.getOutParameter());
    }
}
