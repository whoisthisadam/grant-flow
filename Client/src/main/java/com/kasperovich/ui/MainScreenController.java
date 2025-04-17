package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * Controller for the main screen of the application.
 */
public class MainScreenController {
    private static final Logger logger = LoggerUtil.getLogger(MainScreenController.class);
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button registerButton;
    
    private ClientConnection clientConnection;
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
        updateTexts();
    }
    
    private void updateTexts() {
        loginButton.setText(LangManager.getBundle().getString("login.button"));
        registerButton.setText(LangManager.getBundle().getString("register.button"));
    }
    
    /**
     * Sets the client connection for this controller.
     *
     * @param clientConnection The client connection to set
     */
    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
    
    /**
     * Handles the login button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLoginAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/login_screen.fxml", LangManager.getBundle().getString("login.title"), clientConnection, null);
    }
    
    /**
     * Handles the register button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/register_screen.fxml", LangManager.getBundle().getString("register.title"), clientConnection, null);
    }
    
    @FXML
    private void handleLanguageSwitch() {
        if (LangManager.getLocale().equals(Locale.ENGLISH)) {
            LangManager.setLocale(new Locale("ru"));
        } else {
            LangManager.setLocale(Locale.ENGLISH);
        }
        // Reload screen
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_screen.fxml"), LangManager.getBundle());
            Parent root = loader.load();
            MainScreenController controller = loader.getController();
            controller.setClientConnection(clientConnection);
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
