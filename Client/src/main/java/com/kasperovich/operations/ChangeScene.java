package com.kasperovich.operations;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.Connectionable;
import com.kasperovich.i18n.LangManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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
        Object controller = loader.getController();
        if (controller instanceof com.kasperovich.config.Connectionable) {
            ((com.kasperovich.config.Connectionable) controller).setAccess(access);
        }
        if (user != null) {
            try {
                java.lang.reflect.Method setUser = controller.getClass().getMethod("setUser", user.getClass());
                setUser.invoke(controller, user);
            } catch (Exception ignored) {}
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
