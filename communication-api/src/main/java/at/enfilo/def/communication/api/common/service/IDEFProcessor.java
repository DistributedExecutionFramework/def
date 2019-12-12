package at.enfilo.def.communication.api.common.service;

/**
 * Created by mase on 16.09.2016.
 */
public interface IDEFProcessor<T, U> {

    /**
     * Returns name assigned to this processor.
     * @return processor name.
     */
    String getName();

    /**
     * Returns service / resource implementation instance.
     * @return service / resource implementation instance.
     */
    T getImplementation();

    /**
     * Processor builder function.
     * @return function that builds processor.
     */
    U getProcessor();
}
