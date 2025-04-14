package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.config.ConnectedClientConfig;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.service.AuthenticationService;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Thread responsible for handling client connections.
 */
public class ClientProcessingThread extends Thread {

    private static final Logger logger = LoggerUtil.getLogger(ClientProcessingThread.class);
    private final ConnectedClientConfig clientInfo;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final AuthenticationService authService;
    private Long authenticatedUserId;

    /**
     * Creates a new client processing thread for the given client.
     *
     * @param clientInfo the client configuration
     * @throws IOException if an I/O error occurs
     */
    public ClientProcessingThread(ConnectedClientConfig clientInfo) throws IOException {
        this.clientInfo = clientInfo;
        var socket = clientInfo.getConnectionSocket();
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.authService = AuthenticationService.getInstance();
        logger.debug("Created new client processing thread for client: {}", clientInfo.getConnectionSocket().getInetAddress());
    }

    /**
     * Sends an object to the client.
     *
     * @param object the object to send
     * @throws IOException if an I/O error occurs
     */
    private void sendObject(Serializable object) throws IOException {
        logger.debug("Sending object to client {}: {}", 
            clientInfo.getConnectionSocket().getInetAddress(), object);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    /**
     * Receives an object from the client.
     *
     * @param <T> the type of object to receive
     * @return the received object
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the received object is of an unknown class
     */
    private <T> T receiveObject() throws IOException, ClassNotFoundException {
        T object = (T) objectInputStream.readObject();
        logger.debug("Received object from client {}: {}", 
            clientInfo.getConnectionSocket().getInetAddress(), object);
        return object;
    }

    @Override
    public void run() {
        logger.info("Client processing thread started for client: {}", 
            clientInfo.getConnectionSocket().getInetAddress());
        
        while (true) {
            try {
                startClient();
            } catch (IOException e) {
                logger.error("I/O error while processing client request", e);
                break; // Exit the loop on I/O error as the connection is likely broken
            } catch (ClassNotFoundException e) {
                logger.error("Protocol error while processing client request", e);
                // Continue the loop as this might be a one-time deserialization issue
            } catch (Exception e) {
                logger.error("Unexpected error while processing client request", e);
                break; // Exit the loop on unexpected errors
            }
        }
        
        logger.info("Client processing thread terminated for client: {}", 
            clientInfo.getConnectionSocket().getInetAddress());
    }

    @Override
    public void interrupt() {
        try {
            logger.info("Closing connection to client: {}", 
                clientInfo.getConnectionSocket().getInetAddress());
            clientInfo.getConnectionSocket().close();
        } catch (IOException e) {
            logger.error("Error closing client socket", e);
        }
        super.interrupt();
    }

    /**
     * Returns the client configuration.
     *
     * @return the client configuration
     */
    public ConnectedClientConfig getClientInfo() {
        return clientInfo;
    }

    /**
     * Starts the client processing.
     *
     * @throws Exception if an error occurs
     */
    private void startClient() throws Exception {
        CommandWrapper commandWrapper = receiveObject();
        logger.debug("Processing command: {}", commandWrapper);
        
        // Validate authentication token for commands that require authentication
        if (requiresAuthentication(commandWrapper.getCommand()) && !isAuthenticated(commandWrapper)) {
            logger.warn("Authentication required for command: {}", commandWrapper.getCommand());
            sendObject(new ResponseWrapper(ResponseFromServer.AUTHENTICATION_REQUIRED));
            return;
        }
        
        switch (commandWrapper.getCommand()) {
            case HEALTH_CHECK: {
                logger.debug("Responding to health check with SUCCESS");
                sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS));
                break;
            }
            case LOGIN: {
                handleLogin(commandWrapper);
                break;
            }
            case REGISTER: {
                handleRegistration(commandWrapper);
                break;
            }
            case LOGOUT: {
                handleLogout(commandWrapper);
                break;
            }
            default: {
                logger.warn("Received unknown command: {}", commandWrapper.getCommand());
                sendObject(new ResponseWrapper(ResponseFromServer.UNKNOWN_COMMAND));
            }
        }
    }
    
    /**
     * Handles user login.
     *
     * @param commandWrapper the command wrapper containing login request
     * @throws IOException if an I/O error occurs
     */
    private void handleLogin(CommandWrapper commandWrapper) throws IOException {
        LoginRequest loginRequest = commandWrapper.getData();
        
        if (loginRequest == null) {
            logger.warn("Login request data is missing");
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Login request data is missing"));
            return;
        }
        
        UserDTO user = authService.login(loginRequest);
        
        if (user != null) {
            // Generate token
            String token = authService.generateToken(user.getId());
            
            // Create response
            ResponseWrapper response = new ResponseWrapper(ResponseFromServer.LOGIN_SUCCESS, user);
            response.setAuthToken(token);
            
            // Set authenticated user ID
            authenticatedUserId = user.getId();
            
            logger.info("User logged in successfully: {}", user.getUsername());
            sendObject(response);
        } else {
            logger.warn("Login failed for username: {}", loginRequest.getUsername());
            sendObject(new ResponseWrapper(ResponseFromServer.LOGIN_FAILED, "Invalid username or password"));
        }
    }
    
    /**
     * Handles user registration.
     *
     * @param commandWrapper the command wrapper containing registration request
     * @throws IOException if an I/O error occurs
     */
    private void handleRegistration(CommandWrapper commandWrapper) throws IOException {
        RegistrationRequest registrationRequest = commandWrapper.getData();
        
        if (registrationRequest == null) {
            logger.warn("Registration request data is missing");
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Registration request data is missing"));
            return;
        }
        
        UserDTO user = authService.register(registrationRequest);
        
        if (user != null) {
            // Generate token
            String token = authService.generateToken(user.getId());
            
            // Create response
            ResponseWrapper response = new ResponseWrapper(ResponseFromServer.REGISTRATION_SUCCESS, user);
            response.setAuthToken(token);
            
            // Set authenticated user ID
            authenticatedUserId = user.getId();
            
            logger.info("User registered successfully: {}", user.getUsername());
            sendObject(response);
        } else {
            logger.warn("Registration failed for username: {}", registrationRequest.getUsername());
            sendObject(new ResponseWrapper(ResponseFromServer.REGISTRATION_FAILED_USERNAME_EXISTS, 
                    "Username or email already exists"));
        }
    }
    
    /**
     * Handles user logout.
     *
     * @param commandWrapper the command wrapper containing logout request
     * @throws IOException if an I/O error occurs
     */
    private void handleLogout(CommandWrapper commandWrapper) throws IOException {
        String token = commandWrapper.getAuthToken();
        
        if (token != null) {
            authService.logout(token);
        }
        
        // Clear authenticated user ID
        authenticatedUserId = null;
        
        logger.info("User logged out successfully");
        sendObject(new ResponseWrapper(ResponseFromServer.LOGOUT_SUCCESS));
    }
    
    /**
     * Checks if the command requires authentication.
     *
     * @param command the command to check
     * @return true if the command requires authentication, false otherwise
     */
    private boolean requiresAuthentication(Command command) {
        switch (command) {
            case HEALTH_CHECK:
            case LOGIN:
            case REGISTER:
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Checks if the command wrapper contains a valid authentication token.
     *
     * @param commandWrapper the command wrapper to check
     * @return true if the command wrapper contains a valid authentication token, false otherwise
     */
    private boolean isAuthenticated(CommandWrapper commandWrapper) {
        String token = commandWrapper.getAuthToken();
        
        if (token == null) {
            return false;
        }
        
        Long userId = authService.validateToken(token);
        
        if (userId != null) {
            // Update authenticated user ID
            authenticatedUserId = userId;
            return true;
        }
        
        return false;
    }
}
