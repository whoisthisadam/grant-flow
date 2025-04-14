package com.kasperovich.commands.fromserver;

import java.io.Serializable;

/**
 * Wrapper class for responses sent from server to client.
 * This class wraps a response enum with optional data payload.
 */
public class ResponseWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ResponseFromServer response;
    private final Serializable data;
    private String authToken;
    private String message;
    
    /**
     * Creates a new response wrapper with the specified response and data.
     *
     * @param response the response to wrap
     * @param data the data payload for the response
     */
    public ResponseWrapper(ResponseFromServer response, Serializable data) {
        this.response = response;
        this.data = data;
    }
    
    /**
     * Creates a new response wrapper with the specified response.
     *
     * @param response the response to wrap
     */
    public ResponseWrapper(ResponseFromServer response) {
        this(response, null);
    }
    
    /**
     * Creates a new response wrapper with the specified response and message.
     *
     * @param response the response to wrap
     * @param message a descriptive message
     */
    public ResponseWrapper(ResponseFromServer response, String message, Serializable data) {
        this.response = response;
        this.data = data;
        this.message = message;
    }
    
    /**
     * Gets the response.
     *
     * @return the response
     */
    public ResponseFromServer getResponse() {
        return response;
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
    
    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "ResponseWrapper{" +
                "response=" + response +
                ", hasData=" + (data != null) +
                ", hasAuthToken=" + (authToken != null) +
                ", hasMessage=" + (message != null) +
                '}';
    }
}
