package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.Connectionable;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Setter
@Getter
public abstract class BaseController implements Connectionable {
    protected ClientConnection clientConnection;
    protected UserDTO user;

    // Each controller must provide its FXML file path
    public abstract String getFxmlPath();

    // Each controller must update its texts from the bundle
    public abstract void updateTexts();

    @Override
    public void setAccess(ClientConnection access) {
        this.clientConnection = access;
    }

    @FXML
    protected void handleLanguageSwitch(ActionEvent event) {
        if (LangManager.getLocale().equals(Locale.ENGLISH)) {
            LangManager.setLocale(new Locale("ru"));
        } else {
            LangManager.setLocale(Locale.ENGLISH);
        }
        ChangeScene.changeScene(event, getFxmlPath(), LangManager.getBundle().getString("main.title"), clientConnection, user);
    }

    /**
     * Universal initializer for all controllers. Override in subclasses.
     */
    public void initializeData() {}

}
