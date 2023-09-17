package com.egovframework.javaservice.treeframework.util;


import java.util.logging.Level;
import java.util.logging.Logger;

public class EgovBasicLogger {
    private static final Level IGNORE_INFO_LEVEL;
    private static final Level DEBUG_INFO_LEVEL;
    private static final Level INFO_INFO_LEVEL;
    private static final Logger ignoreLogger;
    private static final Logger debugLogger;
    private static final Logger infoLogger;

    public EgovBasicLogger() {
    }

    public static void ignore(String message, Exception exception) {
        if (exception == null) {
            ignoreLogger.log(IGNORE_INFO_LEVEL, message);
        } else {
            ignoreLogger.log(IGNORE_INFO_LEVEL, message, exception);
        }

    }

    public static void ignore(String message) {
        ignore(message, (Exception)null);
    }

    public static void debug(String message, Exception exception) {
        if (exception == null) {
            debugLogger.log(DEBUG_INFO_LEVEL, message);
        } else {
            debugLogger.log(DEBUG_INFO_LEVEL, message, exception);
        }

    }

    public static void debug(String message) {
        debug(message, (Exception)null);
    }

    public static void info(String message) {
        infoLogger.log(INFO_INFO_LEVEL, message);
    }

    static {
        IGNORE_INFO_LEVEL = Level.OFF;
        DEBUG_INFO_LEVEL = Level.FINEST;
        INFO_INFO_LEVEL = Level.INFO;
        ignoreLogger = Logger.getLogger("ignore");
        debugLogger = Logger.getLogger("debug");
        infoLogger = Logger.getLogger("info");
    }
}
