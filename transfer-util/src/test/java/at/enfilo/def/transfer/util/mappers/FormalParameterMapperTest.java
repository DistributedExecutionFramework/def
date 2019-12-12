package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.transfer.dto.FormalParameterDTO;
import at.enfilo.def.transfer.util.MapperTest;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FormalParameterMapperTest extends MapperTest<FormalParameter, FormalParameterDTO> {

    private static final Object UNSUPPORTED_OBJ;
    private static final Class<?> UNSUPPORTED_CLASS;

    static final FormalParameter FORMAL_PARAMETER;
    static final FormalParameterDTO FORMAL_PARAMETER_DTO;

    static  {
        UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
        UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

        String id = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();

        FORMAL_PARAMETER = new FormalParameter();
        FORMAL_PARAMETER.setId(id);
        FORMAL_PARAMETER.setName(name);
        FORMAL_PARAMETER.setDescription(description);
        FORMAL_PARAMETER.setDataType(DataTypeMapperTest.DATA_TYPE);

        FORMAL_PARAMETER_DTO = new FormalParameterDTO();
        FORMAL_PARAMETER_DTO.setId(id);
        FORMAL_PARAMETER_DTO.setName(name);
        FORMAL_PARAMETER_DTO.setDescription(description);
        FORMAL_PARAMETER_DTO.setDataType(DataTypeMapperTest.DATA_TYPE_DTO);
    }

    public FormalParameterMapperTest() {
        super(FORMAL_PARAMETER, FORMAL_PARAMETER_DTO, FormalParameter.class, FormalParameterDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
    }

    @Test
    public void mapEntityToDTO() throws UnsupportedMappingException {
        FormalParameterDTO formalParameterDTO = MapManager.map(FORMAL_PARAMETER, FormalParameterDTO.class);

        assertNotNull(formalParameterDTO);
        assertEquals(formalParameterDTO.getId(), FORMAL_PARAMETER_DTO.getId());
        assertEquals(formalParameterDTO.getName(), FORMAL_PARAMETER_DTO.getName());
        assertEquals(formalParameterDTO.getDescription(), FORMAL_PARAMETER_DTO.getDescription());
        assertEquals(formalParameterDTO.getDataType(), FORMAL_PARAMETER_DTO.getDataType());
    }

    @Test
    public void mapDTOToEntity() throws UnsupportedMappingException {
        FormalParameter formalParameter = MapManager.map(FORMAL_PARAMETER_DTO, FormalParameter.class);

        assertNotNull(formalParameter);
        assertEquals(formalParameter.getId(), FORMAL_PARAMETER.getId());
        assertEquals(formalParameter.getName(), FORMAL_PARAMETER.getName());
        assertEquals(formalParameter.getDescription(), FORMAL_PARAMETER.getDescription());
        assertEquals(formalParameter.getDataType(), FORMAL_PARAMETER.getDataType());
    }
}
