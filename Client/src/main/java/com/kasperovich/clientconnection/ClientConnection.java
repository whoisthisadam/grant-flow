package com.kasperovich.clientconnection;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientConnection {
    private static final Logger logger = LoggerUtil.getLogger(ClientConnection.class);
    private static final int DEFAULT_TIMEOUT_MS = 3000;
    
    private Socket connectionSocket;
    private final String serverIp;
    private final int serverPort;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String authToken;
    private UserDTO currentUser;

    public ClientConnection(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        logger.debug("ClientConnection initialized with server {}:{}", serverIp, serverPort);
    }

    /**
     * Connects to the server using the configured IP and port.
     * 
     * @return true if connection was successful, false otherwise
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public boolean connectToServer() throws IOException {
        try {
            logger.debug("Attempting to connect to server at {}:{}", serverIp, serverPort);
            connectionSocket = new Socket(serverIp, serverPort);
            connectionSocket.setSoTimeout(DEFAULT_TIMEOUT_MS);
            
            if (!connectionSocket.isConnected()) {
                logger.error("Failed to connect to server at {}:{}", serverIp, serverPort);
                return false;
            }
            
            logger.debug("Socket connected, initializing object streams");
            objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
            logger.debug("Object streams initialized successfully");
            
            return true;
        } catch (IOException e) {
            logger.error("Error connecting to server at {}:{}", serverIp, serverPort, e);
            throw e;
        }
    }

    /**
     * Sends a serializable object to the server.
     * 
     * @param object the object to send
     * @throws IOException if an I/O error occurs when sending the object
     */
    private void sendObject(Serializable object) throws IOException {
        try {
            logger.trace("Sending object of type: {}", object.getClass().getSimpleName());
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            logger.trace("Object sent successfully");
        } catch (IOException e) {
            logger.error("Error sending object to server", e);
            throw e;
        }
    }

    /**
     * Receives an object from the server.
     * 
     * @param <T> the type of object to receive
     * @return the received object
     * @throws IOException if an I/O error occurs when receiving the object
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    private <T> T receiveObject() throws IOException, ClassNotFoundException {
        try {
            logger.trace("Waiting to receive object from server");
            T object = (T) objectInputStream.readObject();
            logger.trace("Received object of type: {}", object.getClass().getSimpleName());
            return object;
        } catch (SocketTimeoutException e) {
            logger.error("Timeout waiting for server response after {} ms", DEFAULT_TIMEOUT_MS, e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("Received object with unknown class", e);
            throw e;
        } catch (IOException e) {
            logger.error("Error receiving object from server", e);
            throw e;
        }
    }

    /**
     * Performs a health check with the server.
     * 
     * @return the response from the server
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public ResponseFromServer healthCheck() throws IOException, ClassNotFoundException {
        try {
            logger.debug("Performing server health check");
            CommandWrapper command = new CommandWrapper(Command.HEALTH_CHECK);
            sendObject(command);
            
            ResponseWrapper response = receiveObject();
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                logger.debug("Health check successful: {}", response.getResponse());
            } else {
                logger.warn("Health check returned non-success response: {}", response.getResponse());
            }
            
            return response.getResponse();
        } catch (Exception e) {
            logger.error("Health check failed with exception", e);
            throw e;
        }
    }
    
    /**
     * Logs in a user with the provided credentials.
     * 
     * @param username the username
     * @param password the password
     * @return the user DTO if login was successful, null otherwise
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public UserDTO login(String username, String password) throws IOException, ClassNotFoundException {
        try {
            logger.debug("Attempting to login user: {}", username);
            LoginRequest loginRequest = new LoginRequest(username, password);
            CommandWrapper command = new CommandWrapper(Command.LOGIN, loginRequest);
            sendObject(command);
            
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.LOGIN_SUCCESS) {
                logger.info("Login successful for user: {}", username);
                this.authToken = response.getAuthToken();
                this.currentUser = response.getData();
                return this.currentUser;
            } else {
                logger.warn("Login failed for user: {}, response: {}", username, response.getResponse());
                return null;
            }
        } catch (Exception e) {
            logger.error("Login failed with exception", e);
            throw e;
        }
    }
    
    /**
     * Registers a new user with the provided information.
     * 
     * @param username the username
     * @param password the password
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param role the user role
     * @return the user DTO if registration was successful, null otherwise
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public UserDTO register(String username, String password, String email, 
                            String firstName, String lastName, String role) 
            throws IOException, ClassNotFoundException {
        try {
            logger.debug("Attempting to register user: {}", username);
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    username, password, email, firstName, lastName, role);
            CommandWrapper command = new CommandWrapper(Command.REGISTER, registrationRequest);
            sendObject(command);
            
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.REGISTRATION_SUCCESS) {
                logger.info("Registration successful for user: {}", username);
                this.authToken = response.getAuthToken();
                this.currentUser = response.getData();
                return this.currentUser;
            } else {
                logger.warn("Registration failed for user: {}, response: {}", username, response.getResponse());
                return null;
            }
        } catch (Exception e) {
            logger.error("Registration failed with exception", e);
            throw e;
        }
    }
    
    /**
     * Logs out the current user.
     * 
     * @return true if logout was successful, false otherwise
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public boolean logout() throws IOException, ClassNotFoundException {
        if (authToken == null) {
            logger.warn("Attempted to logout but no user is logged in");
            return false;
        }
        
        try {
            logger.debug("Attempting to logout user");
            CommandWrapper command = new CommandWrapper(Command.LOGOUT);
            command.setAuthToken(authToken);
            sendObject(command);
            
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.LOGOUT_SUCCESS) {
                logger.info("Logout successful");
                this.authToken = null;
                this.currentUser = null;
                return true;
            } else {
                logger.warn("Logout failed, response: {}", response.getResponse());
                return false;
            }
        } catch (Exception e) {
            logger.error("Logout failed with exception", e);
            throw e;
        }
    }
    
    /**
     * Gets the current authenticated user.
     * 
     * @return the current user, or null if no user is authenticated
     */
    public UserDTO getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authToken != null && currentUser != null;
    }
}
