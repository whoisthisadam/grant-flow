package com.kasperovich.database;

import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for Hibernate SessionFactory management.
 * Provides a singleton instance of the SessionFactory.
 */
public class HibernateUtil {
    private static final Logger logger = LoggerUtil.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private HibernateUtil() {
    }
    
    /**
     * Gets the Hibernate SessionFactory instance.
     * Creates it if it doesn't exist.
     *
     * @return the SessionFactory instance
     */
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                logger.info("Initializing Hibernate SessionFactory");
                
                // Load database properties
                Properties dbProperties = loadDatabaseProperties();
                
                // Create the SessionFactory
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                
                // Set the database properties
                configuration.setProperty("hibernate.connection.url", 
                        configuration.getProperty("hibernate.connection.url")
                                .replace("${db.server}", dbProperties.getProperty("db.server"))
                                .replace("${db.name}", dbProperties.getProperty("db.name")));
                configuration.setProperty("hibernate.connection.username", dbProperties.getProperty("db.username"));
                configuration.setProperty("hibernate.connection.password", dbProperties.getProperty("db.password"));
                
                sessionFactory = configuration.buildSessionFactory();
                logger.info("Hibernate SessionFactory initialized successfully");
            } catch (Exception e) {
                logger.error("Error initializing Hibernate SessionFactory", e);
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }
    
    /**
     * Loads database properties from the configuration file.
     *
     * @return the database properties
     * @throws IOException if the configuration file cannot be read
     */
    private static Properties loadDatabaseProperties() throws IOException {
        logger.debug("Loading database properties from configuration file");
        Properties properties = new Properties();
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
            logger.debug("Database properties loaded successfully");
            return properties;
        } catch (IOException e) {
            logger.error("Error loading database properties", e);
            throw e;
        }
    }
    
    /**
     * Closes the SessionFactory.
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Closing Hibernate SessionFactory");
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed successfully");
        }
    }
}
