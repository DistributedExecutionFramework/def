package at.enfilo.def.transfer.util;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * Created by mase on 26.08.2016.
 */
public abstract class MapperTest<TEntity, TDTO> {

    private final TEntity entity;
    private final TDTO dto;
    private final Class<TEntity> entityClass;
    private final Class<TDTO> dtoClass;
    private final Object unsupportedObj;
    private final Class<?> unsupportedClass;

    public MapperTest(
        TEntity entity, TDTO dto,
        Class<TEntity> entityClass, Class<TDTO> dtoClass,
        Object unsupportedObj, Class<?> unsupportedClass
    ) {
        this.entity = entity;
        this.dto = dto;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        this.unsupportedObj = unsupportedObj;
        this.unsupportedClass = unsupportedClass;
    }

    @Test
    public void mapNullToEntity() throws MapperException {
        TEntity e = MapManager.map((TDTO) null, entityClass);
        assertNull(e);
    }

    @Test
    public void mapNullToDTO() throws MapperException {
        TDTO dto = MapManager.map((TEntity) null, dtoClass);
        assertNull(dto);
    }

    @Test(expected=MapperException.class)
    public void mapEntityToNull()
    throws MapperException {

        Object obj = MapManager.map(entity, null);
    }

    @Test(expected=MapperException.class)
    public void mapDTOToNull()
    throws MapperException {
        Object obj = MapManager.map(dto, null);
    }

    @Test
    public void mapEntityNullToDTONull()
    throws MapperException {
        Object obj = MapManager.map((TEntity) null, (Class<TDTO>) null);
        assertNull(obj);
    }

    @Test
    public void mapDTONullToEntityNull() throws MapperException {
        Object obj = MapManager.map((TDTO) null, (Class<TEntity>) null);
        assertNull(obj);
    }

    @Test(expected=MapperException.class)
    public void mapUnsupportedToEntity()
    throws MapperException {

        TEntity entity = MapManager.map(unsupportedObj, entityClass);
    }

    @Test(expected=MapperException.class)
    public void mapUnsupportedToDTO()
    throws MapperException {

        TDTO dto = MapManager.map(unsupportedObj, dtoClass);
    }

    @Test(expected=MapperException.class)
    public void mapEntityToUnsupported()
    throws MapperException {

        Object obj = MapManager.map(entity, unsupportedClass);
    }

    @Test(expected=MapperException.class)
    public void mapDTOToUnsupported()
    throws MapperException {

        Object obj = MapManager.map(dto, unsupportedClass);
    }
}
