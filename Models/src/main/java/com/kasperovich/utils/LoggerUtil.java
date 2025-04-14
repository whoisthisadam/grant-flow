package com.kasperovich.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging throughout the application.
 * Provides a consistent way to obtain loggers for classes.
 */
public class LoggerUtil {
    
    /**
     * Get a logger for the specified class.
     * 
     * @param clazz The class to get a logger for
     * @return Logger instance for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
    
    /**
     * Get a logger with the specified name.
     * 
     * @param name The name for the logger
     * @return Logger instance with the specified name
     */
    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }
}
