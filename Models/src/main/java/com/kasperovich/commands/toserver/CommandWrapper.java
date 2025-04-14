package com.kasperovich.commands.toserver;

import java.io.Serializable;

/**
 * Base wrapper class for commands sent from client to server.
 * This class wraps a command enum with optional data payload.
 */
public class CommandWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Command command;
    private final Serializable data;
    private String authToken;
    
    /**
     * Creates a new command wrapper with the specified command and data.
     *
     * @param command the command to wrap
     * @param data the data payload for the command
     */
    public CommandWrapper(Command command, Serializable data) {
        this.command = command;
        this.data = data;
    }
    
    /**
     * Creates a new command wrapper with the specified command.
     *
     * @param command the command to wrap
     */
    public CommandWrapper(Command command) {
        this(command, null);
    }
    
    /**
     * Gets the command.
     *
     * @return the command
     */
    public Command getCommand() {
        return command;
    }
    
    /**
     * Gets the data payload.
     *
     * @param <T> the type of the data payload
     * @return the data payload
     */
    public <T extends Serializable> T getData() {
        return (T) data;
    }
    
    /**
     * Gets the authentication token.
     *
     * @return the authentication token
     */
    public String getAuthToken() {
        return authToken;
    }
    
    /**
     * Sets the authentication token.
     *
     * @param authToken the authentication token
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    @Override
    public String toString() {
        return "CommandWrapper{" +
                "command=" + command +
                ", hasData=" + (data != null) +
                ", hasAuthToken=" + (authToken != null) +
                '}';
    }
}
