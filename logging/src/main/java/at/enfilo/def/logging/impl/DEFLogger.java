package at.enfilo.def.logging.impl;

import at.enfilo.def.logging.api.IDEFLogger;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Created by mase on 18.02.2017.
 */
public class DEFLogger implements IDEFLogger {

    private final Logger subjectLogger;

    public DEFLogger(Logger subjectLogger) {
        this.subjectLogger = subjectLogger;
    }

    @Override
    public String getName() {
        return subjectLogger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return subjectLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        subjectLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        subjectLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        subjectLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        subjectLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        subjectLogger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return subjectLogger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        subjectLogger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        subjectLogger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        subjectLogger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        subjectLogger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        subjectLogger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return subjectLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        subjectLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        subjectLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        subjectLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        subjectLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        subjectLogger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return subjectLogger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        subjectLogger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        subjectLogger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        subjectLogger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        subjectLogger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        subjectLogger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return subjectLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        subjectLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        subjectLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        subjectLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        subjectLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        subjectLogger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return subjectLogger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        subjectLogger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        subjectLogger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        subjectLogger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        subjectLogger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        subjectLogger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return subjectLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        subjectLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        subjectLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        subjectLogger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        subjectLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        subjectLogger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return subjectLogger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        subjectLogger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        subjectLogger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        subjectLogger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        subjectLogger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        subjectLogger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return subjectLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        subjectLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        subjectLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        subjectLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        subjectLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        subjectLogger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return subjectLogger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        subjectLogger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        subjectLogger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        subjectLogger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        subjectLogger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        subjectLogger.error(marker, msg, t);
    }
}
