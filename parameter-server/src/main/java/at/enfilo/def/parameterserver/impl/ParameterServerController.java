package at.enfilo.def.parameterserver.impl;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.util.DEFDetector;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.parameterserver.ParameterServer;
import at.enfilo.def.parameterserver.ParameterServerConfiguration;
import at.enfilo.def.parameterserver.api.protocol.IParameterProtocolParser;
import at.enfilo.def.parameterserver.api.protocol.ProtocolParseException;
import at.enfilo.def.parameterserver.impl.store.IStoreDriver;
import at.enfilo.def.parameterserver.impl.store.driver.simple.SimpleStoreDriver;
import at.enfilo.def.parameterserver.impl.type.ITypeActionHandler;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ParameterServerController {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ParameterServerController.class);
    private static final Object INSTANCE_LOCK = new Object();

    private static ParameterServerController instance;

    private final ParameterServerConfiguration config;
    private final IStoreDriver driver;

    private static final Map<ParameterProtocol, IParameterProtocolParser> PROTOCOL_SPECIFIC_PARSERS = new EnumMap<>(ParameterProtocol.class);
    private static final String BASE_PROTOCOL_PARSERS_PACKAGE = "at.enfilo.def.parameterserver.api.protocol";

    private static final Map<String, ITypeActionHandler> TYPE_ACTION_HANDLERS = new HashMap<>();
    private static final String BASE_TYPE_ACTION_HANDLERS_PACKAGE = "at.enfilo.def.parameterserver.impl.type";

    static {
        try {
            DEFDetector.handleSubTypes(
                    IParameterProtocolParser.class,
                    BASE_PROTOCOL_PARSERS_PACKAGE,
                    (IThrowingConsumer<Class<? extends IParameterProtocolParser>>) ParameterServerController::registerProtocolParser
            );
            DEFDetector.handleSubTypes(
                    ITypeActionHandler.class,
                    BASE_TYPE_ACTION_HANDLERS_PACKAGE,
                    (IThrowingConsumer<Class<? extends ITypeActionHandler>>) ParameterServerController::registerTypeActionHandler
            );
        } catch (RuntimeException e) {
            LOGGER.error("Error occurred while initializing parameter parser map.", e);
        }
    }


    private static void registerProtocolParser(Class<? extends IParameterProtocolParser> protocolParserClass) throws IllegalAccessException, InstantiationException {
        IParameterProtocolParser protocolParser = protocolParserClass.newInstance();
        PROTOCOL_SPECIFIC_PARSERS.put(protocolParser.getAssociation(), protocolParser);
        LOGGER.info("{} protocol parser class was successfully registered.", protocolParser.getClass());
    }

    private static void registerTypeActionHandler(Class<? extends ITypeActionHandler> typeActionHandlerClass) throws IllegalAccessException, InstantiationException {
        ITypeActionHandler typeActionHandler = typeActionHandlerClass.newInstance();
        TYPE_ACTION_HANDLERS.put(typeActionHandler.getAssociation(), typeActionHandler);
        LOGGER.info("{} type action handler class was successfully registered.", typeActionHandler.getClass());
    }


    protected ParameterServerController(ParameterServerConfiguration configuration, IStoreDriver storeDriver) {
        config = configuration;
        driver = storeDriver;
    }

    public static ParameterServerController getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                ParameterServerConfiguration configuration = ParameterServer.getInstance().getConfiguration();
                IStoreDriver storeDriver = getStoreDriver(configuration);
                instance = new ParameterServerController(configuration, storeDriver);
                instance.init();
            }
            return instance;
        }
    }

    private static IStoreDriver getStoreDriver(ParameterServerConfiguration configuration) {
        try {
            Class<? extends IStoreDriver> driverClass = Class.forName(configuration.getStoreDriver()).asSubclass(IStoreDriver.class);
            return driverClass.newInstance();

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("Error while loading StoreDriver {}. Using default: SimpleStoreDriver.", configuration.getStoreDriver(), e);
            return new SimpleStoreDriver();
        }
    }

    private void init() {
    }

    public String setParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) {
        try {
            Object data = decode(parameter, protocol);
            return driver.store(programId, parameterId, data, parameter.getDataTypeId());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed setting parameter");
        }
        return null;
    }

    public String createParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol, ParameterType type) {
        try {
            Object data = decode(parameter, protocol);
            return driver.store(programId, parameterId, data, parameter.getDataTypeId(), type);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed creating parameter");
        }
        return null;
    }

    public ResourceDTO getParameter(String programId, String parameterId, ParameterProtocol protocol) {
        try {
            Object data = driver.read(programId, parameterId);
            String typeId = driver.readTypeId(programId, parameterId);
            return encode(data, typeId, protocol);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed getting parameter");
        }
        return null;
    }

    public String addToParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) {
        try {
            if (driver.readTypeId(programId, parameterId).equalsIgnoreCase(parameter.getDataTypeId())
                    && TYPE_ACTION_HANDLERS.containsKey(parameter.getDataTypeId().toLowerCase())) {
                Object data = decode(parameter, protocol);
                Object dataStored = driver.read(programId, parameterId);
                TYPE_ACTION_HANDLERS.get(parameter.getDataTypeId().toLowerCase()).addi(data, dataStored);
                driver.update(programId, parameterId, data);
                return parameterId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed adding parameter");
        }

        return null;
    }

    public String deleteParameter(String programId, String parameterId) {
        if (driver.delete(programId, parameterId)) {
            return parameterId;
        }
        return null;
    }

    public String deleteAllParameters(String programId) {
        if (driver.deleteAll(programId)) {
            return programId;
        }
        return null;
    }

    private Object decode(ResourceDTO parameter, ParameterProtocol protocol) throws ProtocolParseException {
        if (PROTOCOL_SPECIFIC_PARSERS.containsKey(protocol)) {
            return PROTOCOL_SPECIFIC_PARSERS.get(protocol).decode(parameter);
        }
        LOGGER.error("Error decoding parameter. No parser found for protocol {}.", protocol);
        return null;
    }

    private ResourceDTO encode(Object data, String typeId, ParameterProtocol protocol) throws ProtocolParseException {
        if (PROTOCOL_SPECIFIC_PARSERS.containsKey(protocol)) {
            return PROTOCOL_SPECIFIC_PARSERS.get(protocol).encode(data, typeId);
        }
        LOGGER.error("Error encoding parameter. No parser found for protocol {}.", protocol);
        return null;
    }
}
