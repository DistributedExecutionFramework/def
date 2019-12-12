package at.enfilo.def.communication.api.common.util;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;

/**
 * Created by mase on 19.02.2017.
 */
public class ServiceReferenceResolver {

    private static final String PROTOCOL_PLACEHOLDER = "{PROTOCOL}";
    private static final String HOST_PLACEHOLDER = "{HOST}";
    private static final String PORT_PLACEHOLDER = "{PORT}";
    private static final String PATH_PREFIX_PLACEHOLDER = "{PATH_PREFIX}";
    private static final String SERVICE_NAME_PLACEHOLDER = "{SERVICE_NAME}";

    private static final String PROTOCOL_HTTP_VALUE = "http://";
    private static final String VALUE_EMPTY = "";

    private static final String REST_REFERENCE_PATTERN = PROTOCOL_PLACEHOLDER + HOST_PLACEHOLDER + ":" + PORT_PLACEHOLDER + PATH_PREFIX_PLACEHOLDER;
    private static final String THRIFT_HTTP_REFERENCE_PATTERN = REST_REFERENCE_PATTERN + SERVICE_NAME_PLACEHOLDER;
    private static final String THRIFT_TCP_REFERENCE_PATTERN = PATH_PREFIX_PLACEHOLDER + SERVICE_NAME_PLACEHOLDER;

    private ServiceReferenceResolver() {
        // Hiding public constructor
    }

    public static String resolve(ServiceEndpointDTO serviceEndpoint) {
        return resolve(VALUE_EMPTY, serviceEndpoint);
    }

    public static String resolve(String serviceName, ServiceEndpointDTO serviceEndpoint) {
        if (serviceEndpoint != null) {

            String reference = resolveProtocolPart(serviceEndpoint.getProtocol());

            reference = reference.replace(HOST_PLACEHOLDER, serviceEndpoint.getHost());
            reference = reference.replace(PORT_PLACEHOLDER, Integer.toString(serviceEndpoint.getPort()));
            reference = reference.replace(PATH_PREFIX_PLACEHOLDER, serviceEndpoint.getPathPrefix());
            // TODO replace with more efficient replacement (logic)
            reference = reference.replace("*", VALUE_EMPTY);
            reference = reference.replace(SERVICE_NAME_PLACEHOLDER, serviceName);

            return reference;
        }
        return serviceName;
    }

    private static String resolveProtocolPart(Protocol protocol) {
        switch (protocol) {
            case REST: return REST_REFERENCE_PATTERN.replace(PROTOCOL_PLACEHOLDER, PROTOCOL_HTTP_VALUE);

            case THRIFT_HTTP: return THRIFT_HTTP_REFERENCE_PATTERN.replace(PROTOCOL_PLACEHOLDER, PROTOCOL_HTTP_VALUE);

            case THRIFT_TCP: return THRIFT_TCP_REFERENCE_PATTERN.replace(PROTOCOL_PLACEHOLDER, VALUE_EMPTY);

            default: return SERVICE_NAME_PLACEHOLDER;
        }
    }

    /**
     * Convenience method that calculates path prefix by using pattern url.
     *
     * @param urlPattern pattern that specifies how path should look like.
     * @return String path prefix.
     */
//    @Deprecated
//    private String fetchPathPrefix(String urlPattern) {
//        if (urlPattern != null && !urlPattern.isEmpty()) {
//            int endOfPrefixIndex = urlPattern.indexOf(DEFAULT_PATTERN_PLACEHOLDER);
//            return urlPattern.substring(0, endOfPrefixIndex);
//        }
//        return DEFAULT_PATH_PREFIX;
//    }
}
