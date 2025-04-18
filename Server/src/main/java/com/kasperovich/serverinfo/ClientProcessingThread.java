package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.ScholarshipApplicationResponse;
import com.kasperovich.commands.fromserver.ScholarshipApplicationsResponse;
import com.kasperovich.commands.fromserver.AcademicPeriodsResponse;
import com.kasperovich.commands.fromserver.UpdateProfileResponse;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.SubmitScholarshipApplicationCommand;
import com.kasperovich.commands.toserver.UpdateProfileCommand;
import com.kasperovich.config.ConnectedClientConfig;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.service.AuthenticationService;
import com.kasperovich.service.ScholarshipService;
import com.kasperovich.service.ScholarshipApplicationService;
import com.kasperovich.service.AcademicPeriodService;
import com.kasperovich.service.UserService;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread responsible for handling client connections.
 */
public class ClientProcessingThread extends Thread {

    private static final Logger logger = LoggerUtil.getLogger(ClientProcessingThread.class);
    private final ConnectedClientConfig clientInfo;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final AuthenticationService authService;
    private final ScholarshipService scholarshipService;
    private final ScholarshipApplicationService scholarshipApplicationService;
    private final AcademicPeriodService academicPeriodService;
    private final UserService userService;
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
        this.scholarshipService = new ScholarshipService();
        this.scholarshipApplicationService = new ScholarshipApplicationService();
        this.academicPeriodService = new AcademicPeriodService();
        this.userService = new UserService();
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
            case GET_SCHOLARSHIP_PROGRAMS: {
                handleGetScholarshipPrograms(commandWrapper);
                break;
            }
            case APPLY_FOR_SCHOLARSHIP: {
                handleApplyForScholarship(commandWrapper);
                break;
            }
            case GET_USER_APPLICATIONS: {
                handleGetUserApplications(commandWrapper);
                break;
            }
            case GET_ACADEMIC_PERIODS: {
                handleGetAcademicPeriods(commandWrapper);
                break;
            }
            case UPDATE_USER_PROFILE: {
                handleUpdateUserProfile(commandWrapper);
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
        
        if (user == null) {
            logger.warn("Login failed for username: {}", loginRequest.getUsername());
            sendObject(new ResponseWrapper(ResponseFromServer.LOGIN_FAILED, "Invalid username or password"));
            return;
        }
        
        // Generate token
        String token = authService.generateToken(user.getId());
        
        // Create response
        ResponseWrapper response = new ResponseWrapper(ResponseFromServer.LOGIN_SUCCESS, user);
        response.setAuthToken(token);
        
        // Store the authenticated user ID for future requests
        authenticatedUserId = user.getId();
        
        logger.info("User logged in successfully: {}", user.getUsername());
        sendObject(response);
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
        
        // Validate registration data
        if (registrationRequest.getUsername() == null || registrationRequest.getUsername().isEmpty()) {
            logger.warn("Registration failed: username is missing");
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Username is required"));
            return;
        }
        
        if (registrationRequest.getPassword() == null || registrationRequest.getPassword().isEmpty()) {
            logger.warn("Registration failed: password is missing");
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Password is required"));
            return;
        }
        
        // Attempt to register the user
        UserDTO user = authService.register(registrationRequest);
        
        if (user == null) {
            logger.warn("Registration failed for username: {}", registrationRequest.getUsername());
            sendObject(new ResponseWrapper(ResponseFromServer.REGISTRATION_FAILED_USERNAME_EXISTS, "Registration failed. Username may already be taken."));
            return;
        }
        
        // Generate token
        String token = authService.generateToken(user.getId());
        
        // Create response
        ResponseWrapper response = new ResponseWrapper(ResponseFromServer.REGISTRATION_SUCCESS, user);
        response.setAuthToken(token);
        
        // Store the authenticated user ID for future requests
        authenticatedUserId = user.getId();
        
        logger.info("User registered successfully: {}", user.getUsername());
        sendObject(response);
    }
    
    /**
     * Handles user logout.
     *
     * @param commandWrapper the command wrapper containing logout request
     * @throws IOException if an I/O error occurs
     */
    private void handleLogout(CommandWrapper commandWrapper) throws IOException {
        String authToken = commandWrapper.getAuthToken();
        
        if (authToken == null || authToken.isEmpty()) {
            logger.warn("Logout failed: auth token is missing");
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Authentication token is required"));
            return;
        }
        
        // Invalidate the token
        authService.logout(authToken);
        
        // Clear the authenticated user ID
        authenticatedUserId = null;
        
        logger.info("User logged out successfully");
        sendObject(new ResponseWrapper(ResponseFromServer.LOGOUT_SUCCESS));
    }
    
    /**
     * Handles getting scholarship programs.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleGetScholarshipPrograms(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_SCHOLARSHIP_PROGRAMS command");
        
        try {
            // Get all scholarship programs
            List<ScholarshipProgramDTO> programs = scholarshipService.getAllScholarshipPrograms();
            
            // Create response with the programs list wrapped as a serializable ArrayList
            ResponseWrapper response = new ResponseWrapper(ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND, new ArrayList<>(programs));
            
            logger.info("Returning {} scholarship programs", programs.size());
            sendObject(response);
        } catch (Exception e) {
            logger.error("Error getting scholarship programs", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error getting scholarship programs: " + e.getMessage()));
        }
    }
    
    /**
     * Handles applying for a scholarship.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleApplyForScholarship(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling APPLY_FOR_SCHOLARSHIP command");
        
        try {
            // Extract application data
            SubmitScholarshipApplicationCommand command = commandWrapper.getData();
            
            if (command == null) {
                logger.warn("Scholarship application data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Scholarship application data is missing"));
                return;
            }
            
            // Submit the application
            ScholarshipApplicationDTO application = scholarshipApplicationService.submitApplication(
                    authenticatedUserId,
                    command.getProgramId(),
                    command.getPeriodId(),
                    command.getAdditionalInfo()
            );
            
            // Create response with the application
            ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                    true, 
                    "Application submitted successfully", 
                    application
            );
            
            logger.info("Scholarship application submitted successfully. ID: {}", application.getId());
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
        } catch (Exception e) {
            logger.error("Error submitting scholarship application", e);
            ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                    false, 
                    e.getMessage(), 
                    null
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        }
    }
    
    /**
     * Handles getting user applications.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleGetUserApplications(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_USER_APPLICATIONS command");
        
        try {
            // Get user applications
            List<ScholarshipApplicationDTO> applications = scholarshipApplicationService.getUserApplications(authenticatedUserId);
            
            // Create response with the applications list
            ScholarshipApplicationsResponse response = new ScholarshipApplicationsResponse(applications);
            
            logger.info("Returning {} scholarship applications for user {}", applications.size(), authenticatedUserId);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
        } catch (Exception e) {
            logger.error("Error getting user applications", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error getting user applications: " + e.getMessage()));
        }
    }
    
    /**
     * Handles getting academic periods.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleGetAcademicPeriods(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ACADEMIC_PERIODS command");
        
        try {
            // Get academic periods from the database
            List<AcademicPeriodDTO> periods = academicPeriodService.getAllAcademicPeriods();
            
            // Create response with the periods list
            AcademicPeriodsResponse periodsResponse = new AcademicPeriodsResponse(periods);
            ResponseWrapper response = new ResponseWrapper(ResponseFromServer.SUCCESS, periodsResponse);
            
            logger.info("Returning {} academic periods", periods.size());
            sendObject(response);
        } catch (Exception e) {
            logger.error("Error getting academic periods", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error getting academic periods: " + e.getMessage()));
        }
    }
    
    /**
     * Handles updating user profile.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleUpdateUserProfile(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling UPDATE_USER_PROFILE command");
        
        try {
            // Extract profile update data
            UpdateProfileCommand command = commandWrapper.getData();
            
            if (command == null) {
                logger.warn("Profile update data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Profile update data is missing"));
                return;
            }
            
            // Update the user profile
            UserDTO updatedUserDTO = userService.updateUserProfile(
                    authenticatedUserId,
                    command.getUsername(),
                    command.getFirstName(),
                    command.getLastName(),
                    command.getEmail()
            );
            
            if (updatedUserDTO != null) {
                // Create response with the updated user
                UpdateProfileResponse response = new UpdateProfileResponse(
                        true, 
                        "Profile updated successfully", 
                        updatedUserDTO
                );
                
                logger.info("User profile updated successfully. ID: {}", updatedUserDTO.getId());
                sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } else {
                logger.warn("Failed to update user profile");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Failed to update user profile"));
            }
        } catch (IllegalArgumentException e) {
            // Handle specific errors like username already exists
            logger.error("Error updating user profile: {}", e.getMessage());
            UpdateProfileResponse response = new UpdateProfileResponse(
                    false, 
                    e.getMessage(), 
                    null
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            UpdateProfileResponse response = new UpdateProfileResponse(
                    false, 
                    "Error updating user profile: " + e.getMessage(), 
                    null
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        }
    }
    
    /**
     * Checks if the command requires authentication.
     *
     * @param command the command to check
     * @return true if the command requires authentication, false otherwise
     */
    private boolean requiresAuthentication(Command command) {
        // List of commands that require authentication
        return command != Command.LOGIN && 
               command != Command.REGISTER && 
               command != Command.HEALTH_CHECK;
    }
    
    /**
     * Checks if the command wrapper contains a valid authentication token.
     *
     * @param commandWrapper the command wrapper to check
     * @return true if the command wrapper contains a valid authentication token, false otherwise
     */
    private boolean isAuthenticated(CommandWrapper commandWrapper) {
        String authToken = commandWrapper.getAuthToken();
        
        if (authToken == null || authToken.isEmpty()) {
            return false;
        }
        
        // Validate the token
        Long userId = authService.validateToken(authToken);
        
        if (userId != null) {
            // Update authenticated user ID
            authenticatedUserId = userId;
            return true;
        }
        
        return false;
    }
}
