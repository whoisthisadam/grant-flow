package com.kasperovich.database;

import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to test database connectivity.
 */
public class DatabaseConnectionTest {
    private static final Logger logger = LoggerUtil.getLogger(DatabaseConnectionTest.class);

    /**
     * Tests the database connection by executing a simple query.
     * 
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        logger.info("Testing database connection...");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Execute a simple query to test the connection
            Integer result = session.createNativeQuery("SELECT 1", Integer.class)
                                   .getSingleResult();
            
            if (result != null && result == 1) {
                logger.info("Database connection test successful!");
                return true;
            } else {
                logger.error("Database connection test failed: Unexpected result");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Displays database metadata information.
     */
    public static void showDatabaseInfo() {
        logger.info("Retrieving database information...");
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get the JDBC connection from Hibernate
            Connection connection = session.doReturningWork(conn -> {
                // Log basic connection information
                try {
                    DatabaseMetaData metaData = conn.getMetaData();
                    logger.info("Connected to: {} {}", 
                               metaData.getDatabaseProductName(), 
                               metaData.getDatabaseProductVersion());
                    logger.info("JDBC Driver: {} {}", 
                               metaData.getDriverName(), 
                               metaData.getDriverVersion());
                    logger.info("Database URL: {}", metaData.getURL());
                    logger.info("Database User: {}", metaData.getUserName());
                    
                    // List tables
                    List<String> tableNames = new ArrayList<>();
                    try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                        while (tables.next()) {
                            String tableName = tables.getString("TABLE_NAME");
                            tableNames.add(tableName);
                        }
                    }
                    
                    if (tableNames.isEmpty()) {
                        logger.info("No tables found in the database");
                    } else {
                        logger.info("Tables found in the database:");
                        for (String tableName : tableNames) {
                            logger.info("  - {}", tableName);
                        }
                    }
                    
                } catch (SQLException e) {
                    logger.error("Error retrieving database metadata", e);
                }
                
                return conn;
            });
            
        } catch (Exception e) {
            logger.error("Failed to retrieve database information", e);
        }
    }
}
