package at.enfilo.def.logging.api;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.LazyTuple;
import at.enfilo.def.common.impl.Tuple;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by mase 18.02.2017.
 */
public interface IDEFLogger extends Logger {

    // TODO Replace with protected interface method instead (while moving implementation to DEFLogger class), when Java 9 will be released.
    default void intercept(Set<ITuple<ContextIndicator, ?>> contextSet, Consumer<Logger> realFunction) {
        Set<String> controlSet = new HashSet<>();

        try {
            // Filling contextMap with string values
            contextSet.forEach(e -> {
                ContextIndicator contextIndicator = e.getKey();
                String strKey = contextIndicator != null ? contextIndicator.getPlaceholder() : null;

                if (strKey != null) {
                    controlSet.add(strKey);

                    Object rawValue = e.getValue();
                    String strValue = rawValue != null ? rawValue.toString() : null;

                    MDC.put(strKey, strValue);
                }
            });

            // Calling intercepted function.
            realFunction.accept(this);

        } catch (Exception e) {
            error("Error occurs while intercepting logs. [{}]", e.getMessage(), e);
        } finally {

            // Cleaning up.
            controlSet.forEach(MDC::remove);
        }
    }

    // TODO Replace with protected interface method instead (while moving implementation to DEFLogger class), when Java 9 will be released.
    default void intercept(ContextIndicator contextIndicator, String contextValue, Consumer<Logger> realFunction) {
        // Assembling context Set.
        Set<ITuple<ContextIndicator, ?>> contextSet = Collections.singleton(new Tuple<>(contextIndicator, contextValue));

        // Delegating call.
        intercept(contextSet, realFunction);
    }

    // TODO Replace with protected interface method instead (while moving implementation to DEFLogger class), when Java 9 will be released.
    default void intercept(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Consumer<Logger> realFunction) {
        // Assembling context Set.
        Set<ITuple<ContextIndicator, ?>> contextSet = Collections.singleton(new LazyTuple<>(contextIndicator, contextValueSupplier));

        // Delegating call.
        intercept(contextSet, realFunction);
    }



    ////////////////////////////
    // SUPPLIER BASED METHODS //
    ////////////////////////////

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(msg));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(format, arg));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(format, arg1, arg2));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object... arguments) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(format, arguments));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg, Throwable t) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(msg, t));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(marker, msg));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(marker, format, arg));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(marker, format, arg1, arg2));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object... argArray) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(format, marker, format, argArray));
    }

    default void trace(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg, Throwable t) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.trace(marker, msg, t));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(msg));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(format, arg));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(format, arg1, arg2));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object... arguments) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(format, arguments));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg, Throwable t) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(msg, t));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(marker, msg));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(marker, format, arg));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(marker, format, arg1, arg2));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object... arguments) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(marker, format, arguments));
    }

    default void debug(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg, Throwable t) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.debug(marker, msg, t));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(msg));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(format, arg));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(format, arg1, arg2));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object... arguments) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(format, arguments));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg, Throwable t) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(msg, t));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(marker, msg));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(marker, format, arg));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(marker, format, arg1, arg2));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object... arguments) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(marker, format, arguments));
    }

    default void info(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg, Throwable t) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.info(marker, msg, t));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(msg));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(format, arg));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object... arguments) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(format, arguments));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(format, arg1, arg2));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg, Throwable t) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(msg, t));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(marker, msg));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(marker, format, arg));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(marker, format, arg1, arg2));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object... arguments) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(marker, format, arguments));
    }

    default void warn(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg, Throwable t) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.warn(marker, msg, t));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(msg));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(format, arg));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(format, arg1, arg2));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String format, Object... arguments) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(format, arguments));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, String msg, Throwable t) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(msg, t));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(marker, msg));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(marker, format, arg));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(marker, format, arg1, arg2));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String format, Object... arguments) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(marker, format, arguments));
    }

    default void error(ContextIndicator contextIndicator, Supplier<?> contextValueSupplier, Marker marker, String msg, Throwable t) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValueSupplier, (logger) -> logger.error(marker, msg, t));
    }



    /////////////////////////////
    // EXTRACTOR BASED METHODS //
    /////////////////////////////

    default void trace(ContextIndicator contextIndicator, String contextValue, String msg) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.trace(msg));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, String format, Object arg) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.trace(format, arg));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.trace(format, arg1, arg2));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, String format, Object... arguments) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.trace(format, arguments));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, String msg, Throwable t) {
        if (isTraceEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.trace(msg, t));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.trace(marker, msg));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.trace(marker, format, arg));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.trace(marker, format, arg1, arg2));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object... argArray) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.trace(format, marker, format, argArray));
    }

    default void trace(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg, Throwable t) {
        if (isTraceEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.trace(marker, msg, t));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, String msg) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.debug(msg));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, String format, Object arg) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.debug(format, arg));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.debug(format, arg1, arg2));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, String format, Object... arguments) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.debug(format, arguments));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, String msg, Throwable t) {
        if (isDebugEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.debug(msg, t));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.debug(marker, msg));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.debug(marker, format, arg));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.debug(marker, format, arg1, arg2));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object... arguments) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.debug(marker, format, arguments));
    }

    default void debug(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg, Throwable t) {
        if (isDebugEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.debug(marker, msg, t));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, String msg) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.info(msg));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, String format, Object arg) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.info(format, arg));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.info(format, arg1, arg2));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, String format, Object... arguments) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.info(format, arguments));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, String msg, Throwable t) {
        if (isInfoEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.info(msg, t));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.info(marker, msg));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.info(marker, format, arg));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.info(marker, format, arg1, arg2));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object... arguments) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.info(marker, format, arguments));
    }

    default void info(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg, Throwable t) {
        if (isInfoEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.info(marker, msg, t));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, String msg) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.warn(msg));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, String format, Object arg) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.warn(format, arg));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, String format, Object... arguments) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.warn(format, arguments));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.warn(format, arg1, arg2));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, String msg, Throwable t) {
        if (isWarnEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.warn(msg, t));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.warn(marker, msg));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.warn(marker, format, arg));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.warn(marker, format, arg1, arg2));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object... arguments) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.warn(marker, format, arguments));
    }

    default void warn(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg, Throwable t) {
        if (isWarnEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.warn(marker, msg, t));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, String msg) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.error(msg));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, String format, Object arg) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.error(format, arg));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.error(format, arg1, arg2));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, String format, Object... arguments) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.error(format, arguments));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, String msg, Throwable t) {
        if (isErrorEnabled()) intercept(contextIndicator, contextValue, (logger) -> logger.error(msg, t));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.error(marker, msg));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.error(marker, format, arg));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.error(marker, format, arg1, arg2));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, Marker marker, String format, Object... arguments) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.error(marker, format, arguments));
    }

    default void error(ContextIndicator contextIndicator, String contextValue, Marker marker, String msg, Throwable t) {
        if (isErrorEnabled(marker)) intercept(contextIndicator, contextValue, (logger) -> logger.error(marker, msg, t));
    }



    ///////////////////////////////
    // CONTEXT SET BASED METHODS //
    ///////////////////////////////

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, String msg) {
        if (isTraceEnabled()) intercept(contextSet, (logger) -> logger.trace(msg));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg) {
        if (isTraceEnabled()) intercept(contextSet, (logger) -> logger.trace(format, arg));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) intercept(contextSet, (logger) -> logger.trace(format, arg1, arg2));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object... arguments) {
        if (isTraceEnabled()) intercept(contextSet, (logger) -> logger.trace(format, arguments));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, String msg, Throwable t) {
        if (isTraceEnabled()) intercept(contextSet, (logger) -> logger.trace(msg, t));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg) {
        if (isTraceEnabled(marker)) intercept(contextSet, (logger) -> logger.trace(marker, msg));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) intercept(contextSet, (logger) -> logger.trace(marker, format, arg));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) intercept(contextSet, (logger) -> logger.trace(marker, format, arg1, arg2));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object... argArray) {
        if (isTraceEnabled(marker)) intercept(contextSet, (logger) -> logger.trace(format, marker, format, argArray));
    }

    default void trace(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg, Throwable t) {
        if (isTraceEnabled(marker)) intercept(contextSet, (logger) -> logger.trace(marker, msg, t));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, String msg) {
        if (isDebugEnabled()) intercept(contextSet, (logger) -> logger.debug(msg));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg) {
        if (isDebugEnabled()) intercept(contextSet, (logger) -> logger.debug(format, arg));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) intercept(contextSet, (logger) -> logger.debug(format, arg1, arg2));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object... arguments) {
        if (isDebugEnabled()) intercept(contextSet, (logger) -> logger.debug(format, arguments));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, String msg, Throwable t) {
        if (isDebugEnabled()) intercept(contextSet, (logger) -> logger.debug(msg, t));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg) {
        if (isDebugEnabled(marker)) intercept(contextSet, (logger) -> logger.debug(marker, msg));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) intercept(contextSet, (logger) -> logger.debug(marker, format, arg));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) intercept(contextSet, (logger) -> logger.debug(marker, format, arg1, arg2));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object... arguments) {
        if (isDebugEnabled(marker)) intercept(contextSet, (logger) -> logger.debug(marker, format, arguments));
    }

    default void debug(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg, Throwable t) {
        if (isDebugEnabled(marker)) intercept(contextSet, (logger) -> logger.debug(marker, msg, t));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, String msg) {
        if (isInfoEnabled()) intercept(contextSet, (logger) -> logger.info(msg));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg) {
        if (isInfoEnabled()) intercept(contextSet, (logger) -> logger.info(format, arg));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) intercept(contextSet, (logger) -> logger.info(format, arg1, arg2));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object... arguments) {
        if (isInfoEnabled()) intercept(contextSet, (logger) -> logger.info(format, arguments));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, String msg, Throwable t) {
        if (isInfoEnabled()) intercept(contextSet, (logger) -> logger.info(msg, t));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg) {
        if (isInfoEnabled(marker)) intercept(contextSet, (logger) -> logger.info(marker, msg));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) intercept(contextSet, (logger) -> logger.info(marker, format, arg));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) intercept(contextSet, (logger) -> logger.info(marker, format, arg1, arg2));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object... arguments) {
        if (isInfoEnabled(marker)) intercept(contextSet, (logger) -> logger.info(marker, format, arguments));
    }

    default void info(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg, Throwable t) {
        if (isInfoEnabled(marker)) intercept(contextSet, (logger) -> logger.info(marker, msg, t));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, String msg) {
        if (isWarnEnabled()) intercept(contextSet, (logger) -> logger.warn(msg));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg) {
        if (isWarnEnabled()) intercept(contextSet, (logger) -> logger.warn(format, arg));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object... arguments) {
        if (isWarnEnabled()) intercept(contextSet, (logger) -> logger.warn(format, arguments));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) intercept(contextSet, (logger) -> logger.warn(format, arg1, arg2));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, String msg, Throwable t) {
        if (isWarnEnabled()) intercept(contextSet, (logger) -> logger.warn(msg, t));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg) {
        if (isWarnEnabled(marker)) intercept(contextSet, (logger) -> logger.warn(marker, msg));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) intercept(contextSet, (logger) -> logger.warn(marker, format, arg));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) intercept(contextSet, (logger) -> logger.warn(marker, format, arg1, arg2));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object... arguments) {
        if (isWarnEnabled(marker)) intercept(contextSet, (logger) -> logger.warn(marker, format, arguments));
    }

    default void warn(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg, Throwable t) {
        if (isWarnEnabled(marker)) intercept(contextSet, (logger) -> logger.warn(marker, msg, t));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, String msg) {
        if (isErrorEnabled()) intercept(contextSet, (logger) -> logger.error(msg));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg) {
        if (isErrorEnabled()) intercept(contextSet, (logger) -> logger.error(format, arg));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) intercept(contextSet, (logger) -> logger.error(format, arg1, arg2));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, String format, Object... arguments) {
        if (isErrorEnabled()) intercept(contextSet, (logger) -> logger.error(format, arguments));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, String msg, Throwable t) {
        if (isErrorEnabled()) intercept(contextSet, (logger) -> logger.error(msg, t));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg) {
        if (isErrorEnabled(marker)) intercept(contextSet, (logger) -> logger.error(marker, msg));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) intercept(contextSet, (logger) -> logger.error(marker, format, arg));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) intercept(contextSet, (logger) -> logger.error(marker, format, arg1, arg2));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String format, Object... arguments) {
        if (isErrorEnabled(marker)) intercept(contextSet, (logger) -> logger.error(marker, format, arguments));
    }

    default void error(Set<ITuple<ContextIndicator, ?>> contextSet, Marker marker, String msg, Throwable t) {
        if (isErrorEnabled(marker)) intercept(contextSet, (logger) -> logger.error(marker, msg, t));
    }
}
