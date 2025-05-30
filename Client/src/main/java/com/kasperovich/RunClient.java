package com.kasperovich;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.config.AlertManager;
import com.kasperovich.ui.MainScreenController;
import com.kasperovich.utils.LoggerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class RunClient extends Application {
    private static final Logger logger = LoggerUtil.getLogger(RunClient.class);
    
    /**
     * Gets the properties from the configuration file.
     *
     * @return The properties from the configuration file
     * @throws IOException If the configuration file cannot be read
     */
    public static Properties getPropertiesFromConfig() throws IOException {
        var properties = new Properties();
        String propFileName = "D:/bsuir/networkdev/grant-flow/Client/src/main/resources/config.properties";
        var inputStream = new FileInputStream(propFileName);
        if (inputStream == null) throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        properties.load(inputStream);
        logger.debug("Loaded properties from: {}", propFileName);
        return properties;
    }

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Starting scholarship calculation client application...");
        
        try {
            var properties = getPropertiesFromConfig();
            String serverIp = properties.getProperty("serverIp");
            int serverPort = Integer.parseInt(properties.getProperty("serverPort"));
            
            logger.info("Connecting to server at {}:{}", serverIp, serverPort);
            ClientConnection clientConnectionModule = new ClientConnection(serverIp, serverPort);

            var state = clientConnectionModule.connectToServer();
            if (!state) {
                logger.error("Failed to connect to server at {}:{}", serverIp, serverPort);
                AlertManager.showWarningAlert("Cannot connect to server", "");
                return;
            }
            logger.info("Successfully connected to server");

            logger.debug("Performing server health check");
            if (!clientConnectionModule.healthCheck().equals(ResponseFromServer.SUCCESS)) {
                logger.error("Server health check failed");
                throw new RuntimeException("Error during server health check");
            }
            logger.info("Server health check passed");
            
            // Load the main screen FXML
            logger.debug("Loading main screen FXML");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/main_screen.fxml")));
            Parent root = loader.load();
            
            // Get the controller and set the client connection
            MainScreenController mainController = loader.getController();
            mainController.setClientConnection(clientConnectionModule);
            logger.debug("Main screen controller initialized with client connection");

            // Set up and show the stage
            stage.setTitle("Grant Flow");
            Scene scene = new Scene(root, 800, 600);
            
            // Apply CSS
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            logger.debug("Applied application CSS stylesheet");
            
            stage.setScene(scene);
            stage.show();
            logger.info("Client application UI initialized and displayed");
        } catch (Exception e) {
            logger.error("Error starting client application", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Launching client application");
            launch(args);
        } catch (Exception e) {
            logger.error("Unhandled exception in client application", e);
        }
    }
}