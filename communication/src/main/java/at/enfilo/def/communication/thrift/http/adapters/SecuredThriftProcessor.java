package at.enfilo.def.communication.thrift.http.adapters;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.security.annotations.Role;
import at.enfilo.def.security.annotations.SecuredAnnotationProcessor;
import org.apache.thrift.*;
import org.apache.thrift.protocol.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mase on 31.10.16.
 */
public class SecuredThriftProcessor<I> extends TBaseProcessor<I> {

    private static final Map<String, Set<Role>> I_FACE_ANNOTATION_MAP = new HashMap<>();
    private final I iface;

    private String authorizationHeader;

    private SecuredThriftProcessor(I iface, Map<String, ProcessFunction<I, ? extends TBase>> processFunctionMap) {
        super(iface, processFunctionMap);
        this.iface = iface;

        Class<?> annotatedClass = iface.getClass();
        Method[] annotatedMethods = annotatedClass.getDeclaredMethods();

        if (SecuredAnnotationProcessor.isAnnotated(annotatedClass)) {
            I_FACE_ANNOTATION_MAP.put(annotatedClass.getName(), SecuredAnnotationProcessor.extractRoles(annotatedClass));
        }

        // TODO: Here we have to store Generic String (e.g. toGenericString()) or some equivalent that will contain signature (name + parameter types) of the method.
        // P.S. (return type can be ignored due to the fact that java does not allow to implement two methods with same method signature and different return types).
        for (Method annotatedMethod : annotatedMethods) {
            if (SecuredAnnotationProcessor.isAnnotated(annotatedClass, annotatedMethod)) {
                Set<Role> extractedRoles = SecuredAnnotationProcessor.extractRoles(annotatedClass, annotatedMethod);
                I_FACE_ANNOTATION_MAP.put(annotatedMethod.getName(), extractedRoles);
            }
        }
    }

    public static <I extends IResource> SecuredThriftProcessor wrap(ThriftProcessor<I> thriftProcessor) {
        @SuppressWarnings("unchecked") TBaseProcessor<I> baseProcessor = (TBaseProcessor<I>) thriftProcessor.getProcessor();

        return new SecuredThriftProcessor<>(
            thriftProcessor.getImplementation(),
            baseProcessor.getProcessMapView()
        );
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    public boolean process(TProtocol in, TProtocol out)
    throws TException {

        TMessage msg = in.readMessageBegin();
        ProcessFunction<I, ? extends TBase> fn = getProcessMapView().get(msg.name);

        if (fn != null) {

            // CUSTOM CODE STARTS HERE

            fn.getMethodName(); // TODO: is a canonical method name, we still need access to arguments to be sure we are working with a correct method (hint: fn.getEmptyArgsInstance).

            // TODO extract "caller" class (annotatedClass) and "caller" method (class may be extracted from the servlet HTTPRequest and supplied just as authorizationHeader in the worst scenario.)

            // TODO Perform Authentication Check (! if and only if annotated with secured annotation).
//            String userId = AuthenticationModule.authenticate(authorizationHeader);

            // TODO if and only if auth check was performed and was finished with successful authentication -> perform authorization check.
            // FIXME replace null null with "caller" class, and "caller" method.
//            boolean isAuthorised = AuthorizationModule.isAuthorized(userId, null, null);

            // CUSTOM CODE ENDS HERE

            // If false -> Not authorized, if true execute.
            if (true) { // FIXME do not forget to change this line to correct one.
                fn.process(msg.seqid, in, out, iface);
                return true;
            }

            return produceThriftException(msg, in, out, new TApplicationException(
                TApplicationException.INTERNAL_ERROR,
                "Not authorized to execute method: '" + msg.name + "'"
            ));
        }

        return produceThriftException(msg, in, out, new TApplicationException(
            TApplicationException.UNKNOWN_METHOD,
            "Invalid method name: '" + msg.name + "'"
        ));
    }

    private boolean produceThriftException(
        TMessage message,
        TProtocol in,
        TProtocol out,
        TApplicationException applicationException
    ) throws TException {

        TProtocolUtil.skip(in, TType.STRUCT);
        in.readMessageEnd();

        out.writeMessageBegin(new TMessage(message.name, TMessageType.EXCEPTION, message.seqid));
        applicationException.write(out);
        out.writeMessageEnd();
        out.getTransport().flush();

        return true;
    }
}
