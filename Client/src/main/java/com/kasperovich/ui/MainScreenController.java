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
public class MainScreenController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(MainScreenController.class);
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button registerButton;
    
    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        updateTexts();
    }
    
    @Override
    public void updateTexts() {
        loginButton.setText(LangManager.getBundle().getString("login.button"));
        registerButton.setText(LangManager.getBundle().getString("register.button"));
    }
    
    /**
     * Handles the login button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLoginAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/login_screen.fxml", LangManager.getBundle().getString("login.title"), getClientConnection(), null);
    }
    
    /**
     * Handles the register button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/register_screen.fxml", LangManager.getBundle().getString("register.title"), getClientConnection(), null);
    }
    
    @FXML
    public void handleLanguageSwitch(ActionEvent event) {
        super.handleLanguageSwitch(event);
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/main_screen.fxml";
    }
}
