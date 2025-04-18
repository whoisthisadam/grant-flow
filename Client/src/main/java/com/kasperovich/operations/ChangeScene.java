package com.kasperovich.operations;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.ui.BaseController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;

public class ChangeScene {

    /**
     * Universal scene switcher for JavaFX controllers.
     * Loads the specified FXML, injects the resource bundle, sets up the controller with client connection and user if available,
     * and switches the scene on the current stage. Use this to avoid code duplication in all controllers.
     *
     * @param event The triggering ActionEvent (for getting the stage)
     * @param fxmlFile Path to the FXML file (e.g., "/fxml/dashboard_screen.fxml")
     * @param title Window title (localized string)
     * @param access The ClientConnection to inject into the controller
     * @param user The UserDTO to inject if the controller supports it (can be null)
     * @param <T> Controller type
     */
    public static <T> void changeScene(ActionEvent event, String fxmlFile, String title, ClientConnection access, Object user) {
        FXMLLoader loader = new FXMLLoader(ChangeScene.class.getResource(fxmlFile), LangManager.getBundle());
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        BaseController controller = loader.getController();
        if (user != null) {
            try {
                java.lang.reflect.Method setUser = controller.getClass().getMethod("setUser", user.getClass());
                setUser.invoke(controller, user);
            } catch (Exception ignored) {}
        }
        if (controller != null) {
            controller.setAccess(access);
            controller.initializeData();
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    /**
     * Universal scene switcher for JavaFX controllers with additional data.
     * This overload allows passing additional data to the controller via a setter method.
     *
     * @param event The triggering ActionEvent (for getting the stage)
     * @param fxmlFile Path to the FXML file (e.g., "/fxml/dashboard_screen.fxml")
     * @param title Window title (localized string)
     * @param access The ClientConnection to inject into the controller
     * @param user The UserDTO to inject if the controller supports it (can be null)
     * @param additionalData Additional data to inject into the controller
     * @param setterMethodName Name of the setter method to call with the additional data
     * @param <T> Controller type
     * @return The controller instance that was created
     */
    public static <T extends BaseController> T changeSceneWithData(ActionEvent event, String fxmlFile, String title, 
                                                        ClientConnection access, Object user, 
                                                        Object additionalData, String setterMethodName) {
        FXMLLoader loader = new FXMLLoader(ChangeScene.class.getResource(fxmlFile), LangManager.getBundle());
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        T controller = loader.getController();
        
        // Set user if provided
        if (user != null) {
            try {
                Method setUser = controller.getClass().getMethod("setUser", user.getClass());
                setUser.invoke(controller, user);
            } catch (Exception ignored) {}
        }
        
        // Set additional data if provided
        if (additionalData != null && setterMethodName != null) {
            try {
                Method setter = controller.getClass().getMethod(setterMethodName, additionalData.getClass());
                setter.invoke(controller, additionalData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Set client connection and initialize
        if (controller != null) {
            controller.setAccess(access);
            controller.initializeData();
        }
        
        // Set up the scene and stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
        
        return controller;
    }
}
