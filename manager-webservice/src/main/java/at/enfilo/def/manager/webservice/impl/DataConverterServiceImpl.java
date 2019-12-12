package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.manager.webservice.rest.IDataConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConverterServiceImpl implements IDataConverterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataConverterServiceImpl.class);

    @Override
    public String getDataTypeFromId(String dataTypeId) {
        try {
            LOGGER.debug("Fetching name of data type with id '{}'", dataTypeId);
            String name = BaseDataTypeRegistry.getInstance().getById(dataTypeId).getName();
            return name;
        } catch (UnknownDataTypeException e) {
            LOGGER.warn("Not a BaseDataType: {}.", dataTypeId);
            return dataTypeId;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return dataTypeId;
        }
    }
}
