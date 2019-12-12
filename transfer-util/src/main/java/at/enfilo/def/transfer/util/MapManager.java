package at.enfilo.def.transfer.util;

import at.enfilo.def.common.util.DEFDetector;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.util.mappers.impl.AbstractMapper;
import at.enfilo.def.transfer.util.mappers.impl.MapperTuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by mase on 25.08.2016.
 */
public class MapManager {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(MapManager.class);
    private static final String UNKNOWN_MAPPING_MESSAGE_PATTERN = "Mapping direction is not supported for classes: %s -> %s.";
    private static final String UNSPECIFIED_SOURCE = "null";

    private static final Map<MapperTuple<?, ?>, AbstractMapper<?, ?>> MAPPER_MAP = new HashMap<>();

    static {
		try{
            // Initialization of automatically discovered converter classes.
            DEFDetector.handleSubTypes(AbstractMapper.class, MapManager::registerConverter);
		} catch (RuntimeException e) {
			LOGGER.error(
				"Error occurred while mapper initialization for automatically fetched class.",
				e
			);
		}
    }

    private MapManager() {
    	// Hiding public constructor.
    }

    public static <T, R> R map(T source, Class<R> destinationClass) throws MapperException {
    	if (source == null) {
    		return null;
		}
        try {
        	R destination = null;
        	if (destinationClass != null) {
        		try {
					destination = destinationClass.newInstance();
				} catch (InstantiationException e) {
        			LOGGER.debug(
                        "Instantiation of {} class failed, concrete mapper has to instantiate it: {}",
                        destinationClass,
                        e.getMessage()
					);
				}
			}
			return map(source, destinationClass, destination);

        } catch (IllegalAccessException e) {
            throw new MapperException(e);
		}
	}

	public static <T, R> R map(T source, Class<R> destinationClass, R destination)
	throws MapperException {
		if (source == null) {
			return null;
		}
		try {
			Class<?> sourceClass = source.getClass();
			return getConverter(sourceClass, destinationClass).map(source, destination);

		} catch (UnsupportedMappingException | IllegalStateException | IllegalArgumentException e) {
			throw new MapperException(e);
		}
	}

	public static <T, R> Stream<R> map(Stream<T> stream, Class<R> destinationClass)
	throws MapperException {
		return stream != null ? stream.map(t -> map(t, destinationClass)) : Stream.empty();
	}

	public static <T, R> Stream<R> map(Collection<T> collection, Class<R> destinationClass)
	throws MapperException {
		return collection != null ? map(collection.stream(), destinationClass) : Stream.empty();
	}

	public static void registerConverter(Class<? extends AbstractMapper> abstractConverter)
	throws IllegalAccessException, InstantiationException {
		registerConverter(abstractConverter.newInstance());
	}

	public static void registerConverter(AbstractMapper<?, ?> abstractConverter) {
		MAPPER_MAP.put(abstractConverter.getAssociation(), abstractConverter);
		LOGGER.info(
            "{} mapper class was successfully registered by MapManager.",
            abstractConverter.getClass()
        );
	}

    private static <T, R> AbstractMapper<T, R> getConverter(Class<?> sourceClass, Class<R> destinationClass)
    throws UnsupportedMappingException {

        MapperTuple<?, R> mapperTuple = MapperTuple.wrap(sourceClass, destinationClass);
        AbstractMapper<?, ?> abstractConverter = MAPPER_MAP.get(mapperTuple);

        if (abstractConverter != null) {
            // At this point we can be sure everything is ok.
            // Hash is calculated based on source and destination classes and thus converter function is 100% type secure.
            // If such kind of converter from source class to destination class does not exist, null will be returned.
            @SuppressWarnings("unchecked")
			AbstractMapper<T, R> requestedConverter = (AbstractMapper<T, R>) abstractConverter;
            return requestedConverter;
        }

        String unknownMappingMessage = String.format(UNKNOWN_MAPPING_MESSAGE_PATTERN, sourceClass, destinationClass);
        throw new UnsupportedMappingException(unknownMappingMessage);
    }
}
