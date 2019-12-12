package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.common.util.DEFDetector;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.HashMap;

public class ShellOutputFormatter {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ShellOutputFormatter.class);
    private static final HashMap<Class<?>, ShellFormatter<?>> FORMATTER_MAP = new HashMap<>();

	static {
        try{
            // Initialization of automatically discovered formatter classes.
            DEFDetector.handleSubTypes(ShellFormatter.class, ShellOutputFormatter::registerFormatter);
        } catch (RuntimeException e) {
            LOGGER.error(
                "Error occurred while formatter initialization for automatically fetched class.",
                e
            );
        }
	}

    private ShellOutputFormatter() {
    }

	public static String format(Object o) {
		return format(o, 0);
	}

	public static String format(Object o, int shift) {
		if (FORMATTER_MAP.containsKey(o.getClass())) {
			return FORMATTER_MAP.get(o.getClass()).format(o, shift);
		}
		return o.toString();
	}

    public static void registerFormatter(Class<? extends ShellFormatter> abstractFormatter)
    throws IllegalAccessException, InstantiationException {
        registerFormatter(abstractFormatter.newInstance());
    }

    public static void registerFormatter(ShellFormatter<?> abstractFormatter) {
        FORMATTER_MAP.put(abstractFormatter.getAssociation(), abstractFormatter);
        LOGGER.debug(
            "{} formatter class was successfully registered by ShellOutputFormatter.",
            abstractFormatter.getClass()
        );
    }
}
