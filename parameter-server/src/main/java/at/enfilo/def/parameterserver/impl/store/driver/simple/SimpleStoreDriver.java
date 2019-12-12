package at.enfilo.def.parameterserver.impl.store.driver.simple;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.parameterserver.impl.store.IStoreDriver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleStoreDriver implements IStoreDriver {

    private Map<String, Map<String, StorageEntry>> dataStore = new ConcurrentHashMap<>();
    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SimpleStoreDriver.class);

    @Override
    public boolean exists(String programId, String parameterId) {
        if (!dataStore.containsKey(programId)) {
            return false;
        }
        return dataStore.get(programId).containsKey(parameterId);
    }

    @Override
    public Object read(String programId, String parameterId) {
        LOGGER.info("Trying to read parameter with programId: {} and parameterId: {}.", programId, parameterId);
        if (exists(programId, parameterId)) {
            return dataStore.get(programId).get(parameterId).getData();
        }
        LOGGER.error("Parameter with programId: {} and parameterId: {} does not exist.", programId, parameterId);
        return null;
    }

    @Override
    public String readTypeId(String programId, String parameterId) {
        LOGGER.info("Trying to read type id or parameter with programId: {} and parameterId: {}.", programId, parameterId);
        if (exists(programId, parameterId)) {
            return dataStore.get(programId).get(parameterId).getTypeId();
        }
        LOGGER.error("Parameter with programId: {} and parameterId: {} does not exist.", programId, parameterId);
        return null;
    }

    @Override
    public String store(String programId, String parameterId, Object data, String typeId) {
        LOGGER.info("Trying to store parameter of type {} with programId: {} and parameterId: {} and typeId: {}", ParameterType.READ_WRITE, programId, parameterId, typeId);
        StorageEntry entry = new StorageEntry(data, typeId);
        if (!dataStore.containsKey(programId)) {
            LOGGER.info("Creating new data store for programId: {}.", programId);
            dataStore.put(programId, new ConcurrentHashMap<>());
        }
        if (exists(programId, parameterId)) {
            dataStore.get(programId).replace(parameterId, entry);
        } else {
            dataStore.get(programId).put(parameterId, entry);
        }
        LOGGER.info("Stored parameter of type {} with programId: {} and parameterId: {} and typeId: {}", ParameterType.READ_WRITE, programId, parameterId, typeId);
        return parameterId;
    }

    @Override
    public String store(String programId, String parameterId, Object data, String typeId, ParameterType type) {
        LOGGER.info("Trying to store parameter of type {} with programId: {} and parameterId: {} and typeId: {}", type, programId, parameterId, typeId);

        if (type == ParameterType.READ_WRITE) {
            return store(programId, parameterId, data, typeId);
        }
        LOGGER.info("Type {} not recognized, falling back to default READ_WRITE type.", type);
        return store(programId, parameterId, data, typeId);
    }

    @Override
    public String update(String programId, String parameterId, Object data) {
        LOGGER.info("Trying to update parameter with programId: {} and parameterId: {}.", programId, parameterId);
        if (!exists(programId, parameterId)) {
            LOGGER.error("Parameter with programId: {} and parameterId: {} does not exist.", programId, parameterId);
            return null;
        }
        dataStore.get(programId).get(parameterId).setData(data);
        LOGGER.info("Updated parameter with programId: {} and parameterId: {}.", programId, parameterId);
        return parameterId;
    }

    @Override
    public boolean delete(String programId, String parameterId) {
        LOGGER.info("Trying to delete parameter with programId: {} and parameterId: {}.", programId, parameterId);
        if (exists(programId, parameterId)) {
            dataStore.get(programId).remove(parameterId);
            LOGGER.info("Deleted parameter with programId: {} and parameterId: {}.", programId, parameterId);
            return true;
        }
        LOGGER.info("No parameter found with programId: {} and parameterId: {}.", programId, parameterId);
        return false;
    }

    @Override
    public boolean deleteAll(String programId) {
        LOGGER.info("Trying to delete all parameters with programId: {}.",programId);
        if (dataStore.containsKey(programId)) {
            int nrOfParameters = dataStore.get(programId).size();
            dataStore.remove(programId);
            LOGGER.info("Deleted {} parameters with programId: {}.", nrOfParameters, programId);
            return true;
        }
        LOGGER.info("No parameters found with programId: {}.",programId);
        return false;
    }
}
