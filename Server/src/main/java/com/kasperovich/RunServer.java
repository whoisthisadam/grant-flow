package com.kasperovich;

import com.kasperovich.database.DatabaseConnectionTest;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.serverinfo.ServerProcessingThread;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class RunServer {

    private static int port;
    private static ServerProcessingThread serverProcessingThread;
    private static final Logger logger = LoggerUtil.getLogger(RunServer.class);
    
    private static final Thread.UncaughtExceptionHandler exceptionHandler = (t, ex) -> {
        logger.error("Uncaught exception in thread: " + t.getName(), ex);
    };

    /**
     * Gets the properties from the configuration file.
     *
     * @return The properties from the configuration file
     * @throws IOException If the configuration file cannot be read
     */
    private static Properties getPropertiesFromConfig() throws IOException {
        var properties = new Properties();
        String propFileName = "src/main/resources/config.properties";
        
        // Try to load from the current directory first
        File configFile = new File(propFileName);
        if (!configFile.exists()) {
            // If not found, try from the Server directory
            configFile = new File("Server/" + propFileName);
        }
        
        logger.debug("Looking for configuration file at: {}", configFile.getAbsolutePath());
        
        if (!configFile.exists()) {
            throw new FileNotFoundException("Property file not found at: " + configFile.getAbsolutePath());
        }
        
        try (var inputStream = new FileInputStream(configFile)) {
            properties.load(inputStream);
            logger.debug("Loaded properties from: {}", configFile.getAbsolutePath());
            return properties;
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Starting scholarship calculation server...");
            
            var properties = getPropertiesFromConfig();
            port = Integer.parseInt(properties.getProperty("serverPort"));
            logger.info("Server configured to run on port: {}", port);

            // Test database connection if requested
            boolean testDb = args.length > 0 && args[0].equalsIgnoreCase("--test-db");
            if (testDb) {
                logger.info("Testing database connection...");
                boolean connectionSuccessful = DatabaseConnectionTest.testConnection();
                if (connectionSuccessful) {
                    DatabaseConnectionTest.showDatabaseInfo();
                    logger.info("Database connection test completed. Exiting without starting server.");
                    // Shutdown Hibernate before exiting
                    HibernateUtil.shutdown();
                    return;
                } else {
                    logger.warn("Database connection test failed. Server will not start.");
                    return;
                }
            } else {
                // For normal server operation, test the database connection but continue regardless
                logger.info("Testing database connection...");
                boolean connectionSuccessful = DatabaseConnectionTest.testConnection();
                if (connectionSuccessful) {
                    DatabaseConnectionTest.showDatabaseInfo();
                } else {
                    logger.warn("Database connection test failed. Server will start, but database functionality may not work properly.");
                }
            }

            serverProcessingThread = new ServerProcessingThread(port);
            serverProcessingThread.setName("Server processing thread");
            serverProcessingThread.setUncaughtExceptionHandler(exceptionHandler);

            logger.info("Starting server processing thread");
            serverProcessingThread.start();
            logger.info("Server successfully started and listening on port: {}", port);
            
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            // Ensure Hibernate is properly shut down
            HibernateUtil.shutdown();
            System.exit(1);
        }
    }
}