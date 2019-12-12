package at.enfilo.def.library.api.util;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.impl.AbstractBaseRegistry;
import at.enfilo.def.common.util.DEFDetector;
import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Holds all BaseDataTypes.
 */
public class BaseDataTypeRegistry extends AbstractBaseRegistry<String, DataTypeDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(BaseDataTypeRegistry.class);

    private static final String BASE_DATATYPE_PACKAGE = "at.enfilo.def.datatype";
    private static final String THRIFT_EXTENSION = ".thrift";

    /**
     * Private class to provide thread safe singleton.
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final BaseDataTypeRegistry INSTANCE = new BaseDataTypeRegistry();
        private ThreadSafeLazySingletonWrapper() {}
    }

    /**
	 * Hide constructor, use static methods.
	 */
	private BaseDataTypeRegistry() {
	    super();
	}

    /**
     * Singleton pattern.
     * @return an instance of BaseDataTypeRegistry
     */
    public static BaseDataTypeRegistry getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    @Override
	public void refresh() {
        try {
            // Initialization of automatically discovered formatter classes.
            DEFDetector.handleSubTypes(
                TBase.class,
                BASE_DATATYPE_PACKAGE,
                (IThrowingConsumer<Class<? extends TBase>>) this::createDataType
            );
        } catch (RuntimeException e) {
            LOGGER.error(
                "Error occurred while registering automatically fetched DEF data-type.",
                e
            );
        }
    }

	/**
	 * Returns registered data-type if exists.
     *
	 * @param name - name of data-type.
	 * @return registered data-type if exists.
	 * @throws UnknownDataTypeException in case no data-type with given name was found.
	 */
	public DataTypeDTO getDataTypeByName(String name) throws UnknownDataTypeException {
		if (containsKey(name)) {
			return get(name);
		}
		throw new UnknownDataTypeException("DataType with name " + name + " not registered.");
	}

	/**
	 * Returns registered data-type if exists.
     *
	 * @param id - id of data-type.
	 * @return registered data-type if exists.
	 * @throws UnknownDataTypeException in case no data-type with given id was found.
	 */
	public DataTypeDTO getById(String id)
	throws UnknownDataTypeException {
        return getAll().stream().filter(dto -> dto.getId().equals(id)).findFirst().orElseThrow(() ->
            new UnknownDataTypeException("DataType with id " + id + " not registered.")
        );
	}

    /**
     * Create a DataType for a given Class.
     *
     * @param dataTypeClass - data-type class.
     * @throws IOException - if error occurs while trying to create datatype.
     */
    private void createDataType(Class<? extends TBase> dataTypeClass)
	throws IOException, IllegalAccessException, InstantiationException {
        LOGGER.info("Create DataType for class {}", dataTypeClass);

        DataTypeDTO dataType = new DataTypeDTO();
        TBase instance = dataTypeClass.newInstance();
        TFieldIdEnum idField = instance.fieldForId((short)1); // Convention: field with id 1 is '_id'
		String id = instance.getFieldValue(idField).toString();
        dataType.setId(id);
        dataType.setName(dataTypeClass.getSimpleName());

        StringBuilder schema = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            dataTypeClass.getClassLoader().getResourceAsStream(dataTypeClass.getSimpleName() + THRIFT_EXTENSION)
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                schema.append(line);
                schema.append("\n");
            }
        }

        dataType.setSchema(schema.toString());

        put(dataType.getName(), dataType);
        LOGGER.debug(
            "{} data-type class was successfully registered by BaseDataTypeRegistry as {}",
            dataTypeClass,
            dataType.getId()
        );
    }
}
