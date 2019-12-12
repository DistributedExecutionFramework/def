package at.enfilo.def.logging.impl;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by mase on 18.02.2017.
 */
public class DEFLoggerFactory {

    private DEFLoggerFactory() {
        // Hiding public constructor.
    }

    /**
     * Return a logger named corresponding to the class passed as parameter,
     * using the statically bound {@link ILoggerFactory} instance.
     *
     * Delegates all requests to real SLF4J implementation.
     */
    public static IDEFLogger getLogger(Class<?> clazz) {
        return new DEFLogger(LoggerFactory.getLogger(clazz));
    }

    /**
     * Return a logger named according to the name parameter using the
     * statically bound {@link ILoggerFactory} instance.
     *
     * Delegates all requests to real SLF4J implementation.
     */
    public static IDEFLogger getLogger(String name) {
        return new DEFLogger(LoggerFactory.getLogger(name));
    }


    public static Set<ITuple<ContextIndicator, ?>> createProgramContext(String pId) {
    	return new ContextSetBuilder()
				.add(ContextIndicator.PROGRAM_CONTEXT, pId)
				.build();
	}

	public static Set<ITuple<ContextIndicator, ?>> createJobContext(String pId, String jId) {
		return new ContextSetBuilder()
				.add(ContextIndicator.PROGRAM_CONTEXT, pId)
				.add(ContextIndicator.JOB_CONTEXT, jId)
				.build();
	}

	public static Set<ITuple<ContextIndicator, ?>> createJobContext(String jId) {
		return new ContextSetBuilder()
				.add(ContextIndicator.JOB_CONTEXT, jId)
				.build();
	}

	public static Set<ITuple<ContextIndicator, ?>> createTaskContext(String pId, String jId, String tId) {
		return new ContextSetBuilder()
				.add(ContextIndicator.PROGRAM_CONTEXT, pId)
				.add(ContextIndicator.JOB_CONTEXT, jId)
				.add(ContextIndicator.TASK_CONTEXT, tId)
				.build();
	}

	public static Set<ITuple<ContextIndicator, ?>> createTaskContext(String tId) {
		return new ContextSetBuilder()
				.add(ContextIndicator.TASK_CONTEXT, tId)
				.build();
	}

}
