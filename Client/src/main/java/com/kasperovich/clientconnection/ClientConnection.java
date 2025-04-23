package com.kasperovich.clientconnection;

import com.kasperovich.commands.fromserver.*;
import com.kasperovich.commands.toserver.*;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.report.ApplicationStatusDTO;
import com.kasperovich.dto.report.ScholarshipDistributionDTO;
import com.kasperovich.dto.report.UserActivityDTO;
import com.kasperovich.dto.scholarship.*;
import com.kasperovich.entities.BudgetStatus;
import com.kasperovich.entities.UserRole;
import com.kasperovich.utils.LoggerUtil;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientConnection {
    private static final Logger logger = LoggerUtil.getLogger(ClientConnection.class);
    private static final int DEFAULT_TIMEOUT_MS = 10000;
    
    private Socket connectionSocket;
    private final String serverIp;
    private final int serverPort;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String authToken;
    @Getter
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
     * Gets a list of available scholarship programs from the server.
     * 
     * @return a list of scholarship programs
     * @throws Exception if an error occurs
     */
    public List<ScholarshipProgramDTO> getScholarshipPrograms() throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get scholarship programs but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        logger.debug("Getting scholarship programs from server");
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_PROGRAMS);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND) {
                @SuppressWarnings("unchecked")
                List<ScholarshipProgramDTO> programs = responseWrapper.getData();
                logger.debug("Received {} scholarship programs", programs.size());
                return programs;
            } else {
                logger.error("Error getting scholarship programs: {}", responseWrapper.getMessage());
                throw new Exception("Error getting scholarship programs: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting scholarship programs", e);
            throw new Exception("Error getting scholarship programs: " + e.getMessage());
        }
    }

    /**
     * Gets a list of available academic periods from the server.
     * 
     * @return a list of academic periods
     */
    public List<AcademicPeriodDTO> getAcademicPeriods() {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get academic periods but no user is authenticated");
            return new ArrayList<>();
        }
        
        try {
            logger.debug("Getting academic periods from server");
            
            // Create and send command to get academic periods
            CommandWrapper command = new CommandWrapper(Command.GET_ACADEMIC_PERIODS);
            command.setAuthToken(authToken);
            sendObject(command);
            
            // Receive response
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                // Extract periods from response
                AcademicPeriodsResponse periodsResponse = response.getData();
                
                if (periodsResponse != null && periodsResponse.getPeriods() != null) {
                    List<AcademicPeriodDTO> periods = periodsResponse.getPeriods();
                    logger.info("Retrieved {} academic periods from server", periods.size());
                    return periods;
                } else {
                    logger.warn("Received empty periods response");
                    return new ArrayList<>();
                }
            } else {
                logger.warn("Failed to get academic periods: {}", response.getResponse());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Error getting academic periods", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Submits a scholarship application.
     * 
     * @param programId the ID of the scholarship program
     * @param periodId the ID of the academic period
     * @param additionalInfo additional information for the application
     * @return the submitted application if successful, null otherwise
     * @throws Exception if the server returns an error message
     */
    public ScholarshipApplicationDTO submitScholarshipApplication(Long programId, Long periodId, String additionalInfo) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to submit scholarship application but no user is authenticated");
            return null;
        }
        
        try {
            logger.debug("Submitting scholarship application for program {} and period {}", programId, periodId);
            
            // Create and send command to submit scholarship application
            SubmitScholarshipApplicationCommand command = new SubmitScholarshipApplicationCommand();
            command.setProgramId(programId);
            command.setPeriodId(periodId);
            command.setAdditionalInfo(additionalInfo);
            
            CommandWrapper wrapper = new CommandWrapper(Command.APPLY_FOR_SCHOLARSHIP, command);
            wrapper.setAuthToken(authToken);
            
            sendObject(wrapper);
            
            // Receive response
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                // Extract application from response
                ScholarshipApplicationResponse applicationResponse = response.getData();
                
                if (applicationResponse != null && applicationResponse.isSuccess()) {
                    ScholarshipApplicationDTO application = applicationResponse.getApplication();
                    logger.info("Successfully submitted scholarship application for program {} and period {}", 
                        programId, periodId);
                    return application;
                } else {
                    String errorMessage = applicationResponse != null ? applicationResponse.getMessage() : "Unknown error";
                    logger.warn("Failed to submit scholarship application: {}", errorMessage);
                    throw new Exception(errorMessage);
                }
            } else {
                String errorMessage = response.getData() != null ? response.getData().toString() : "Unknown error";
                logger.warn("Failed to submit scholarship application: {}", errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error submitting scholarship application", e);
            throw e;
        }
    }
    
    /**
     * Gets a list of scholarship applications for the current user.
     * 
     * @return a list of scholarship applications
     */
    public List<ScholarshipApplicationDTO> getUserApplications() {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get user applications but no user is authenticated");
            return new ArrayList<>();
        }
        
        try {
            logger.debug("Getting scholarship applications for user {}", currentUser.getUsername());
            
            // Create and send command to get user applications
            CommandWrapper command = new CommandWrapper(Command.GET_USER_APPLICATIONS);
            command.setAuthToken(authToken);
            sendObject(command);
            
            // Receive response
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                // Extract applications from response
                ScholarshipApplicationsResponse applicationsResponse = response.getData();
                
                if (applicationsResponse != null && applicationsResponse.getApplications() != null) {
                    List<ScholarshipApplicationDTO> applications = applicationsResponse.getApplications();
                    logger.info("Retrieved {} scholarship applications for user {}", 
                        applications.size(), currentUser.getUsername());
                    return applications;
                } else {
                    logger.warn("Received empty applications response");
                    return new ArrayList<>();
                }
            } else {
                logger.warn("Failed to get user applications: {}", response.getResponse());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Error getting user applications", e);
            return new ArrayList<>();
        }
    }

    /**
     * Updates the current user's profile with the provided information.
     * Only the fields that are not null will be updated.
     * 
     * @param username the new username, or null to keep current
     * @param firstName the new first name, or null to keep current
     * @param lastName the new last name, or null to keep current
     * @param email the new email, or null to keep current
     * @return the updated user DTO if successful, null if update failed
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     * @throws Exception if the server returns an error message
     */
    public UserDTO updateUserProfile(String username, String firstName, String lastName, String email) 
            throws IOException, ClassNotFoundException, Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to update user profile but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        try {
            logger.debug("Updating user profile for user ID: {}", currentUser.getId());
            
            // Create the update profile command with only the fields to update
            UpdateProfileCommand command = new UpdateProfileCommand();
            command.setUsername(username);
            command.setFirstName(firstName);
            command.setLastName(lastName);
            command.setEmail(email);
            
            // Create and send command wrapper
            CommandWrapper wrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, command);
            wrapper.setAuthToken(authToken);
            
            sendObject(wrapper);
            
            // Receive response
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                // Extract updated user from response
                UpdateProfileResponse updateResponse = response.getData();
                
                if (updateResponse != null && updateResponse.isSuccess()) {
                    UserDTO updatedUser = updateResponse.getUser();
                    
                    // Update the current user
                    this.currentUser = updatedUser;
                    
                    logger.info("User profile updated successfully for user ID: {}", updatedUser.getId());
                    return updatedUser;
                } else {
                    String errorMessage = updateResponse != null ? updateResponse.getMessage() : "Unknown error";
                    logger.warn("Failed to update user profile: {}", errorMessage);
                    throw new Exception(errorMessage);
                }
            } else {
                String errorMessage = response.getMessage();
                logger.warn("Failed to update user profile: {}", errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            throw e;
        }
    }

    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authToken != null && currentUser != null;
    }

    /**
     * Gets all scholarship programs.
     * 
     * @return a list of all scholarship programs
     * @throws Exception if an error occurs
     */
    public List<ScholarshipProgramDTO> getAllScholarshipPrograms() throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get all scholarship programs but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        logger.debug("Getting all scholarship programs");
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_PROGRAMS);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND) {
                @SuppressWarnings("unchecked")
                List<ScholarshipProgramDTO> programs = (List<ScholarshipProgramDTO>) responseWrapper.getData();
                logger.debug("Received {} scholarship programs", programs.size());
                return programs;
            } else {
                logger.error("Error getting scholarship programs: {}", responseWrapper.getMessage());
                throw new Exception("Error getting scholarship programs: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting scholarship programs", e);
            throw new Exception("Error getting scholarship programs: " + e.getMessage());
        }
    }
    
    /**
     * Gets active scholarship programs.
     * 
     * @return a list of active scholarship programs
     * @throws Exception if an error occurs
     */
    public List<ScholarshipProgramDTO> getActiveScholarshipPrograms() throws Exception {
        List<ScholarshipProgramDTO> allPrograms = getAllScholarshipPrograms();
        
        logger.debug("Filtering active scholarship programs");
        return allPrograms.stream()
                .filter(ScholarshipProgramDTO::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new scholarship program.
     * 
     * @param command the command containing the scholarship program data
     * @return the created scholarship program
     * @throws Exception if an error occurs
     */
    public ScholarshipProgramDTO createScholarshipProgram(CreateScholarshipProgramCommand command) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to create scholarship program but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        // Check if user is admin
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to create scholarship program: {}", currentUser.getUsername());
            throw new Exception("Only administrators can create scholarship programs");
        }
        
        logger.debug("Creating new scholarship program: {}", command.getName());
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_SCHOLARSHIP_PROGRAM, command);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                ScholarshipProgramOperationResponse response = (ScholarshipProgramOperationResponse) responseWrapper.getData();
                
                if (response.isSuccess()) {
                    logger.info("Created new scholarship program: {}", response.getProgram().getName());
                    return response.getProgram();
                } else {
                    logger.error("Error creating scholarship program: {}", response.getMessage());
                    throw new Exception(response.getMessage());
                }
            } else {
                logger.error("Error creating scholarship program: {}", responseWrapper.getMessage());
                throw new Exception("Error creating scholarship program: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error creating scholarship program", e);
            throw new Exception("Error creating scholarship program: " + e.getMessage());
        }
    }
    
    /**
     * Updates an existing scholarship program.
     * 
     * @param command the command containing the updated scholarship program data
     * @return the updated scholarship program
     * @throws Exception if an error occurs
     */
    public ScholarshipProgramDTO updateScholarshipProgram(UpdateScholarshipProgramCommand command) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to update scholarship program but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        // Check if user is admin
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to update scholarship program: {}", currentUser.getUsername());
            throw new Exception("Only administrators can update scholarship programs");
        }
        
        logger.debug("Updating scholarship program with ID: {}", command.getId());
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_SCHOLARSHIP_PROGRAM, command);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                ScholarshipProgramOperationResponse response = (ScholarshipProgramOperationResponse) responseWrapper.getData();
                
                if (response.isSuccess()) {
                    logger.info("Updated scholarship program: {}", response.getProgram().getName());
                    return response.getProgram();
                } else {
                    logger.error("Error updating scholarship program: {}", response.getMessage());
                    throw new Exception(response.getMessage());
                }
            } else {
                logger.error("Error updating scholarship program: {}", responseWrapper.getMessage());
                throw new Exception("Error updating scholarship program: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error updating scholarship program", e);
            throw new Exception("Error updating scholarship program: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a scholarship program.
     * 
     * @param programId the ID of the scholarship program to delete
     * @return true if the program was deleted, false otherwise
     * @throws Exception if an error occurs
     */
    public boolean deleteScholarshipProgram(Long programId) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to delete scholarship program but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        // Check if user is admin
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to delete scholarship program: {}", currentUser.getUsername());
            throw new Exception("Only administrators can delete scholarship programs");
        }
        
        logger.debug("Deleting scholarship program with ID: {}", programId);
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.DELETE_SCHOLARSHIP_PROGRAM, programId);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                ScholarshipProgramOperationResponse response = (ScholarshipProgramOperationResponse) responseWrapper.getData();
                
                if (response.isSuccess()) {
                    logger.info("Deleted scholarship program with ID: {}", programId);
                    return true;
                } else {
                    logger.error("Error deleting scholarship program: {}", response.getMessage());
                    throw new Exception(response.getMessage());
                }
            } else {
                logger.error("Error deleting scholarship program: {}", responseWrapper.getMessage());
                throw new Exception("Error deleting scholarship program: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error deleting scholarship program", e);
            throw new Exception("Error deleting scholarship program: " + e.getMessage());
        }
    }

    /**
     * Gets all pending scholarship applications.
     * Only administrators can use this method.
     *
     * @return a list of pending scholarship applications
     * @throws Exception if an error occurs or the user is not authorized
     */
    public List<ScholarshipApplicationDTO> getPendingApplications() throws Exception {
        logger.debug("Getting pending scholarship applications");
        
        if (authToken == null) {
            throw new Exception("You must be logged in to view pending applications");
        }
        
        try {
            GetPendingApplicationsCommand command = new GetPendingApplicationsCommand();
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_PENDING_APPLICATIONS, command);
            commandWrapper.setAuthToken(authToken);
            
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.ERROR) {
                String errorMessage = "Failed to get pending applications";
                if (response.getData() instanceof ApplicationsResponse) {
                    ApplicationsResponse appResponse = (ApplicationsResponse) response.getData();
                    errorMessage = appResponse.getErrorMessage();
                }
                logger.warn(errorMessage);
                throw new Exception(errorMessage);
            }
            
            if (response.getData() instanceof ApplicationsResponse) {
                ApplicationsResponse appResponse = response.getData();
                logger.info("Retrieved {} pending applications", appResponse.getApplications().size());
                return appResponse.getApplications();
            } else {
                throw new Exception("Unexpected response type");
            }
        } catch (IOException e) {
            logger.error("Error getting pending applications", e);
            throw new Exception("Error connecting to server: " + e.getMessage());
        }
    }

    /**
     * Gets all scholarship applications.
     * Only administrators can use this method.
     *
     * @return a list of all scholarship applications
     * @throws Exception if an error occurs or the user is not authorized
     */
    public List<ScholarshipApplicationDTO> getAllApplications() throws Exception {
        logger.debug("Getting all scholarship applications");
        
        if (authToken == null) {
            throw new Exception("You must be logged in to view all applications");
        }
        
        try {
            GetAllApplicationsCommand command = new GetAllApplicationsCommand();
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ALL_APPLICATIONS, command);
            commandWrapper.setAuthToken(authToken);
            
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.ERROR) {
                String errorMessage = "Failed to get all applications";
                if (response.getData() instanceof ApplicationsResponse) {
                    ApplicationsResponse appResponse = response.getData();
                    errorMessage = appResponse.getErrorMessage();
                }
                logger.warn(errorMessage);
                throw new Exception(errorMessage);
            }
            
            if (response.getData() instanceof ApplicationsResponse) {
                ApplicationsResponse appResponse = (ApplicationsResponse) response.getData();
                logger.info("Retrieved {} applications", appResponse.getApplications().size());
                return appResponse.getApplications();
            } else {
                throw new Exception("Unexpected response type");
            }
        } catch (IOException e) {
            logger.error("Error getting all applications", e);
            throw new Exception("Error connecting to server: " + e.getMessage());
        }
    }
    
    /**
     * Approves a scholarship application.
     * Only administrators can use this method.
     *
     * @param applicationId the ID of the application to approve
     * @param comments comments about the approval decision
     * @return the updated application
     * @throws Exception if an error occurs or the user is not authorized
     */
    public ScholarshipApplicationDTO approveApplication(Long applicationId, String comments) throws Exception {
        logger.debug("Approving scholarship application with ID: {}", applicationId);
        
        if (authToken == null) {
            throw new Exception("You must be logged in to approve applications");
        }
        
        try {
            ApproveApplicationCommand command = new ApproveApplicationCommand(applicationId, comments);
            CommandWrapper commandWrapper = new CommandWrapper(Command.APPROVE_APPLICATION, command);
            commandWrapper.setAuthToken(authToken);
            
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.ERROR) {
                String errorMessage = "Failed to approve application";
                if (response.getData() instanceof ApplicationReviewResponse) {
                    ApplicationReviewResponse appResponse = (ApplicationReviewResponse) response.getData();
                    errorMessage = appResponse.getErrorMessage();
                }
                logger.warn(errorMessage);
                throw new Exception(errorMessage);
            }
            
            if (response.getData() instanceof ApplicationReviewResponse) {
                ApplicationReviewResponse appResponse = response.getData();
                logger.info("Application with ID: {} has been approved", applicationId);
                return appResponse.getApplication();
            } else {
                throw new Exception("Unexpected response type");
            }
        } catch (IOException e) {
            logger.error("Error approving application", e);
            throw new Exception("Error connecting to server: " + e.getMessage());
        }
    }
    
    /**
     * Rejects a scholarship application.
     * Only administrators can use this method.
     *
     * @param applicationId the ID of the application to reject
     * @param comments comments about the rejection decision
     * @return the updated application
     * @throws Exception if an error occurs or the user is not authorized
     */
    public ScholarshipApplicationDTO rejectApplication(Long applicationId, String comments) throws Exception {
        logger.debug("Rejecting scholarship application with ID: {}", applicationId);
        
        if (authToken == null) {
            throw new Exception("You must be logged in to reject applications");
        }
        
        try {
            RejectApplicationCommand command = new RejectApplicationCommand(applicationId, comments);
            CommandWrapper commandWrapper = new CommandWrapper(Command.REJECT_APPLICATION, command);
            commandWrapper.setAuthToken(authToken);
            
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.ERROR) {
                String errorMessage = "Failed to reject application";
                if (response.getData() instanceof ApplicationReviewResponse) {
                    ApplicationReviewResponse appResponse = response.getData();
                    errorMessage = appResponse.getErrorMessage();
                }
                logger.warn(errorMessage);
                throw new Exception(errorMessage);
            }
            
            if (response.getData() instanceof ApplicationReviewResponse) {
                ApplicationReviewResponse appResponse = response.getData();
                logger.info("Application with ID: {} has been rejected", applicationId);
                return appResponse.getApplication();
            } else {
                throw new Exception("Unexpected response type");
            }
        } catch (IOException e) {
            logger.error("Error rejecting application", e);
            throw new Exception("Error connecting to server: " + e.getMessage());
        }
    }

    // Fund Management Methods
    
    /**
     * Gets all budgets from the server.
     * 
     * @return a list of budgets
     * @throws Exception if an error occurs
     */
    public List<BudgetDTO> getAllBudgets() throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get all budgets but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get all budgets");
            throw new Exception("Only administrators can access budget information");
        }
        
        logger.debug("Getting all budgets from server");
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ALL_BUDGETS);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetsResponse budgetsResponse = responseWrapper.getData();
                
                if (budgetsResponse != null && budgetsResponse.isSuccess()) {
                    List<BudgetDTO> budgets = budgetsResponse.getBudgets();
                    logger.debug("Received {} budgets", budgets.size());
                    return budgets;
                } else {
                    String errorMessage = budgetsResponse != null ? budgetsResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error getting budgets: {}", errorMessage);
                    throw new Exception("Error getting budgets: " + errorMessage);
                }
            } else {
                logger.error("Error getting budgets: {}", responseWrapper.getMessage());
                throw new Exception("Error getting budgets: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting budgets", e);
            throw new Exception("Error getting budgets: " + e.getMessage());
        }
    }
    
    /**
     * Gets the active budget from the server.
     * 
     * @return the active budget, or null if no budget is active
     * @throws Exception if an error occurs
     */
    public BudgetDTO getActiveBudget() throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get active budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get active budget");
            throw new Exception("Only administrators can access budget information");
        }
        
        logger.debug("Getting active budget from server");
        
        try {
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACTIVE_BUDGET);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetResponse budgetResponse = responseWrapper.getData();
                
                if (budgetResponse != null && budgetResponse.isSuccess()) {
                    BudgetDTO budget = budgetResponse.getBudget();
                    logger.debug("Received active budget: {}", budget != null ? budget.getId() : "none");
                    return budget;
                } else {
                    String errorMessage = budgetResponse != null ? budgetResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error getting active budget: {}", errorMessage);
                    throw new Exception("Error getting active budget: " + errorMessage);
                }
            } else {
                logger.error("Error getting active budget: {}", responseWrapper.getMessage());
                throw new Exception("Error getting active budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting active budget", e);
            throw new Exception("Error getting active budget: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new budget.
     * 
     * @param fiscalYear the fiscal year
     * @param fiscalPeriod the fiscal period
     * @param totalAmount the total amount
     * @param startDate the start date
     * @param endDate the end date
     * @param description the description
     * @return the created budget
     * @throws Exception if an error occurs
     */
    public BudgetDTO createBudget(Integer fiscalYear, String fiscalPeriod, BigDecimal totalAmount, 
                                 LocalDate startDate, LocalDate endDate, String description) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to create budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to create budget");
            throw new Exception("Only administrators can create budgets");
        }
        
        logger.debug("Creating new budget for fiscal year: {}", fiscalYear);
        
        try {
            // Create command data
            CreateBudgetCommand createBudgetCommand = new CreateBudgetCommand(
                fiscalYear, fiscalPeriod, totalAmount, startDate, endDate, description);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_BUDGET, createBudgetCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetResponse budgetResponse = responseWrapper.getData();
                
                if (budgetResponse != null && budgetResponse.isSuccess()) {
                    BudgetDTO budget = budgetResponse.getBudget();
                    logger.info("Budget created successfully. ID: {}", budget.getId());
                    return budget;
                } else {
                    String errorMessage = budgetResponse != null ? budgetResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error creating budget: {}", errorMessage);
                    throw new Exception("Error creating budget: " + errorMessage);
                }
            } else {
                logger.error("Error creating budget: {}", responseWrapper.getMessage());
                throw new Exception("Error creating budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error creating budget", e);
            throw new Exception("Error creating budget: " + e.getMessage());
        }
    }
    
    /**
     * Updates an existing budget.
     * 
     * @param id the budget ID
     * @param fiscalYear the fiscal year
     * @param fiscalPeriod the fiscal period
     * @param totalAmount the total amount
     * @param startDate the start date
     * @param endDate the end date
     * @param description the description
     * @param status the budget status
     * @return the updated budget
     * @throws Exception if an error occurs
     */
    public BudgetDTO updateBudget(Long id, Integer fiscalYear, String fiscalPeriod, BigDecimal totalAmount,
                                 LocalDate startDate, LocalDate endDate, String description, BudgetStatus status) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to update budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to update budget");
            throw new Exception("Only administrators can update budgets");
        }
        
        logger.debug("Updating budget with ID: {}", id);
        
        try {
            // Create command data
            UpdateBudgetCommand updateBudgetCommand = new UpdateBudgetCommand(
                id, fiscalYear, fiscalPeriod, totalAmount, startDate, endDate, description, status);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_BUDGET, updateBudgetCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetResponse budgetResponse = responseWrapper.getData();
                
                if (budgetResponse != null && budgetResponse.isSuccess()) {
                    BudgetDTO budget = budgetResponse.getBudget();
                    logger.info("Budget updated successfully. ID: {}", budget.getId());
                    return budget;
                } else {
                    String errorMessage = budgetResponse != null ? budgetResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error updating budget: {}", errorMessage);
                    throw new Exception("Error updating budget: " + errorMessage);
                }
            } else {
                logger.error("Error updating budget: {}", responseWrapper.getMessage());
                throw new Exception("Error updating budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error updating budget", e);
            throw new Exception("Error updating budget: " + e.getMessage());
        }
    }
    
    /**
     * Activates a budget.
     * 
     * @param budgetId the budget ID
     * @return the activated budget
     * @throws Exception if an error occurs
     */
    public BudgetDTO activateBudget(Long budgetId) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to activate budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to activate budget");
            throw new Exception("Only administrators can activate budgets");
        }
        
        logger.debug("Activating budget with ID: {}", budgetId);
        
        try {
            // Create command data
            ActivateBudgetCommand activateBudgetCommand = new ActivateBudgetCommand(budgetId);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.ACTIVATE_BUDGET, activateBudgetCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetResponse budgetResponse = responseWrapper.getData();
                
                if (budgetResponse != null && budgetResponse.isSuccess()) {
                    BudgetDTO budget = budgetResponse.getBudget();
                    logger.info("Budget activated successfully. ID: {}", budget.getId());
                    return budget;
                } else {
                    String errorMessage = budgetResponse != null ? budgetResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error activating budget: {}", errorMessage);
                    throw new Exception("Error activating budget: " + errorMessage);
                }
            } else {
                logger.error("Error activating budget: {}", responseWrapper.getMessage());
                throw new Exception("Error activating budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error activating budget", e);
            throw new Exception("Error activating budget: " + e.getMessage());
        }
    }
    
    /**
     * Closes a budget.
     * 
     * @param budgetId the budget ID
     * @return the closed budget
     * @throws Exception if an error occurs
     */
    public BudgetDTO closeBudget(Long budgetId) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to close budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to close budget");
            throw new Exception("Only administrators can close budgets");
        }
        
        logger.debug("Closing budget with ID: {}", budgetId);
        
        try {
            // Create command data
            CloseBudgetCommand closeBudgetCommand = new CloseBudgetCommand(budgetId);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.CLOSE_BUDGET, closeBudgetCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                BudgetResponse budgetResponse = responseWrapper.getData();
                
                if (budgetResponse != null && budgetResponse.isSuccess()) {
                    BudgetDTO budget = budgetResponse.getBudget();
                    logger.info("Budget closed successfully. ID: {}", budget.getId());
                    return budget;
                } else {
                    String errorMessage = budgetResponse != null ? budgetResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error closing budget: {}", errorMessage);
                    throw new Exception("Error closing budget: " + errorMessage);
                }
            } else {
                logger.error("Error closing budget: {}", responseWrapper.getMessage());
                throw new Exception("Error closing budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error closing budget", e);
            throw new Exception("Error closing budget: " + e.getMessage());
        }
    }
    
    /**
     * Allocates funds from a budget to a scholarship program.
     * 
     * @param budgetId the budget ID
     * @param programId the scholarship program ID
     * @param amount the amount to allocate
     * @param notes optional notes about the allocation
     * @return the fund allocation
     * @throws Exception if an error occurs
     */
    public FundAllocationDTO allocateFunds(Long budgetId, Long programId, BigDecimal amount, String notes) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to allocate funds but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to allocate funds");
            throw new Exception("Only administrators can allocate funds");
        }
        
        logger.debug("Allocating {} funds from budget {} to program {}", amount, budgetId, programId);
        
        try {
            // Create command data
            AllocateFundsCommand allocateFundsCommand = new AllocateFundsCommand(budgetId, programId, amount, notes);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.ALLOCATE_FUNDS, allocateFundsCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                FundAllocationResponse allocationResponse = responseWrapper.getData();
                
                if (allocationResponse != null && allocationResponse.isSuccess()) {
                    FundAllocationDTO allocation = allocationResponse.getAllocation();
                    logger.info("Funds allocated successfully. ID: {}", allocation.getId());
                    return allocation;
                } else {
                    String errorMessage = allocationResponse != null ? allocationResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error allocating funds: {}", errorMessage);
                    throw new Exception("Error allocating funds: " + errorMessage);
                }
            } else {
                logger.error("Error allocating funds: {}", responseWrapper.getMessage());
                throw new Exception("Error allocating funds: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error allocating funds", e);
            throw new Exception("Error allocating funds: " + e.getMessage());
        }
    }
    
    /**
     * Gets all fund allocations for a specific budget.
     * 
     * @param budgetId the budget ID
     * @return a list of fund allocations
     * @throws Exception if an error occurs
     */
    public List<FundAllocationDTO> getAllocationsByBudget(Long budgetId) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get allocations by budget but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get allocations by budget");
            throw new Exception("Only administrators can access fund allocation information");
        }
        
        logger.debug("Getting fund allocations for budget: {}", budgetId);
        
        try {
            // Create command data
            GetAllocationsByBudgetCommand getAllocationsByBudgetCommand = new GetAllocationsByBudgetCommand(budgetId);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ALLOCATIONS_BY_BUDGET, getAllocationsByBudgetCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                FundAllocationsResponse allocationsResponse = responseWrapper.getData();
                
                if (allocationsResponse != null && allocationsResponse.isSuccess()) {
                    List<FundAllocationDTO> allocations = allocationsResponse.getAllocations();
                    logger.debug("Received {} fund allocations for budget {}", allocations.size(), budgetId);
                    return allocations;
                } else {
                    String errorMessage = allocationsResponse != null ? allocationsResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error getting allocations by budget: {}", errorMessage);
                    throw new Exception("Error getting allocations by budget: " + errorMessage);
                }
            } else {
                logger.error("Error getting allocations by budget: {}", responseWrapper.getMessage());
                throw new Exception("Error getting allocations by budget: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting allocations by budget", e);
            throw new Exception("Error getting allocations by budget: " + e.getMessage());
        }
    }
    
    /**
     * Gets all fund allocations for a specific scholarship program.
     * 
     * @param programId the scholarship program ID
     * @return a list of fund allocations
     * @throws Exception if an error occurs
     */
    public List<FundAllocationDTO> getAllocationsByProgram(Long programId) throws Exception {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get allocations by program but no user is authenticated");
            throw new Exception("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get allocations by program");
            throw new Exception("Only administrators can access fund allocation information");
        }
        
        logger.debug("Getting fund allocations for program: {}", programId);
        
        try {
            // Create command data
            GetAllocationsByProgramCommand getAllocationsByProgramCommand = new GetAllocationsByProgramCommand(programId);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ALLOCATIONS_BY_PROGRAM, getAllocationsByProgramCommand);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                FundAllocationsResponse allocationsResponse = responseWrapper.getData();
                
                if (allocationsResponse != null && allocationsResponse.isSuccess()) {
                    List<FundAllocationDTO> allocations = allocationsResponse.getAllocations();
                    logger.debug("Received {} fund allocations for program {}", allocations.size(), programId);
                    return allocations;
                } else {
                    String errorMessage = allocationsResponse != null ? allocationsResponse.getErrorMessage() : "Unknown error";
                    logger.error("Error getting allocations by program: {}", errorMessage);
                    throw new Exception("Error getting allocations by program: " + errorMessage);
                }
            } else {
                logger.error("Error getting allocations by program: {}", responseWrapper.getMessage());
                throw new Exception("Error getting allocations by program: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting allocations by program", e);
            throw new Exception("Error getting allocations by program: " + e.getMessage());
        }
    }

    /**
     * Gets all academic periods.
     *
     * @return list of all academic periods
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public List<AcademicPeriodDTO> getAllAcademicPeriods() throws IOException, ClassNotFoundException {
        logger.debug("Getting all academic periods");
        
        GetAcademicPeriodsCommand command = new GetAcademicPeriodsCommand(false);
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACADEMIC_PERIODS, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                AcademicPeriodsResponse periodsResponse = response.getData();
                if (periodsResponse != null) {
                    List<AcademicPeriodDTO> periods = periodsResponse.getPeriods();
                    logger.debug("Received {} academic periods", periods.size());
                    return periods;
                }
            }
            
            logger.warn("Failed to get academic periods: {}", response.getResponse());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error getting academic periods", e);
            throw e;
        }
    }
    
    /**
     * Gets only active academic periods.
     *
     * @return list of active academic periods
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public List<AcademicPeriodDTO> getActiveAcademicPeriods() throws IOException, ClassNotFoundException {
        logger.debug("Getting active academic periods");
        
        GetAcademicPeriodsCommand command = new GetAcademicPeriodsCommand(true);
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACADEMIC_PERIODS, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                AcademicPeriodsResponse periodsResponse = response.getData();
                if (periodsResponse != null) {
                    List<AcademicPeriodDTO> periods = periodsResponse.getPeriods();
                    logger.debug("Received {} active academic periods", periods.size());
                    return periods;
                }
            }
            
            logger.warn("Failed to get active academic periods: {}", response.getResponse());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error getting active academic periods", e);
            throw e;
        }
    }
    
    /**
     * Creates a new academic period.
     *
     * @param periodDTO the academic period to create
     * @return the created academic period with ID
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public AcademicPeriodDTO createAcademicPeriod(AcademicPeriodDTO periodDTO) throws IOException, ClassNotFoundException {
        logger.debug("Creating academic period: {}", periodDTO.getName());
        
        CreateAcademicPeriodCommand command = new CreateAcademicPeriodCommand(periodDTO);
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_ACADEMIC_PERIOD, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                AcademicPeriodResponse periodResponse = response.getData();
                if (periodResponse != null) {
                    AcademicPeriodDTO createdPeriod = periodResponse.getPeriod();
                    logger.debug("Created academic period with ID: {}", createdPeriod.getId());
                    return createdPeriod;
                }
            }
            
            String errorMessage = "Failed to create academic period";
            if (response.getResponse() == ResponseFromServer.ERROR) {
                errorMessage = response.getMessage();
            }
            logger.warn(errorMessage);
            throw new IOException(errorMessage);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error creating academic period", e);
            throw e;
        }
    }
    
    /**
     * Updates an existing academic period.
     *
     * @param periodDTO the academic period to update
     * @return the updated academic period
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public AcademicPeriodDTO updateAcademicPeriod(AcademicPeriodDTO periodDTO) throws IOException, ClassNotFoundException {
        logger.debug("Updating academic period with ID: {}", periodDTO.getId());
        
        UpdateAcademicPeriodCommand command = new UpdateAcademicPeriodCommand(periodDTO);
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_ACADEMIC_PERIOD, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                AcademicPeriodResponse periodResponse = response.getData();
                if (periodResponse != null) {
                    AcademicPeriodDTO updatedPeriod = periodResponse.getPeriod();
                    logger.debug("Updated academic period with ID: {}", updatedPeriod.getId());
                    return updatedPeriod;
                }
            }
            
            String errorMessage = "Failed to update academic period";
            if (response.getResponse() == ResponseFromServer.ERROR) {
                errorMessage = response.getMessage();
            }
            logger.warn(errorMessage);
            throw new IOException(errorMessage);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error updating academic period", e);
            throw e;
        }
    }
    
    /**
     * Updates the status of an academic period.
     *
     * @param periodId the ID of the academic period
     * @param active the new active status
     * @return the updated academic period
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public AcademicPeriodDTO updateAcademicPeriodStatus(Long periodId, boolean active) throws IOException, ClassNotFoundException {
        logger.debug("Updating academic period status: ID={}, active={}", periodId, active);
        
        UpdateAcademicPeriodStatusCommand command = new UpdateAcademicPeriodStatusCommand(periodId, active);
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_ACADEMIC_PERIOD_STATUS, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                AcademicPeriodResponse periodResponse = response.getData();
                if (periodResponse != null) {
                    AcademicPeriodDTO updatedPeriod = periodResponse.getPeriod();
                    logger.debug("Updated academic period status: ID={}, active={}", updatedPeriod.getId(), updatedPeriod.isActive());
                    return updatedPeriod;
                }
            }
            
            String errorMessage = "Failed to update academic period status";
            if (response.getResponse() == ResponseFromServer.ERROR) {
                errorMessage = response.getMessage();
            }
            logger.warn(errorMessage);
            throw new IOException(errorMessage);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error updating academic period status", e);
            throw e;
        }
    }
    
    /**
     * Deletes an academic period.
     *
     * @param periodId the ID of the academic period to delete
     * @return true if deleted successfully
     * @throws IOException if communication error occurs
     * @throws ClassNotFoundException if the class of the received object cannot be found
     */
    public boolean deleteAcademicPeriod(Long periodId) throws IOException, ClassNotFoundException {
        logger.debug("Deleting academic period with ID: {}", periodId);
        
        DeleteAcademicPeriodCommand command = new DeleteAcademicPeriodCommand(periodId);
        CommandWrapper commandWrapper = new CommandWrapper(Command.DELETE_ACADEMIC_PERIOD, command);
        commandWrapper.setAuthToken(authToken);
        
        try {
            sendObject(commandWrapper);
            ResponseWrapper response = receiveObject();
            
            if (response.getResponse() == ResponseFromServer.SUCCESS) {
                logger.debug("Deleted academic period with ID: {}", periodId);
                return true;
            }
            
            String errorMessage = "Failed to delete academic period";
            if (response.getResponse() == ResponseFromServer.ERROR) {
                errorMessage = response.getMessage();
            }
            logger.warn(errorMessage);
            throw new IOException(errorMessage);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error deleting academic period", e);
            throw e;
        }
    }

    /**
     * Gets scholarship distribution report
     *
     * @param startDate Start date for report period
     * @param endDate End date for report period
     * @return List of ScholarshipDistributionDTO objects
     * @throws IOException if communication error occurs
     */
    public List<ScholarshipDistributionDTO> getScholarshipDistributionReport(LocalDate startDate, LocalDate endDate) throws IOException {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get scholarship distribution report but no user is authenticated");
            throw new IOException("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get scholarship distribution report");
            throw new IOException("Only administrators can access reports");
        }
        
        logger.debug("Getting scholarship distribution report from {} to {}", startDate, endDate);
        
        try {
            // Create command
            GetScholarshipDistributionReportCommand command = new GetScholarshipDistributionReportCommand(startDate, endDate);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_DISTRIBUTION_REPORT, command);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                ScholarshipDistributionReportResponse reportResponse = responseWrapper.getData();
                
                if (reportResponse != null) {
                    List<ScholarshipDistributionDTO> reportData = reportResponse.getReportData();
                    logger.debug("Received scholarship distribution report with {} entries", reportData.size());
                    return reportData;
                } else {
                    logger.error("Received null report response");
                    throw new IOException("Error getting scholarship distribution report: Null response");
                }
            } else {
                logger.error("Error getting scholarship distribution report: {}", responseWrapper.getMessage());
                throw new IOException("Error getting scholarship distribution report: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting scholarship distribution report", e);
            throw new IOException("Error getting scholarship distribution report: " + e.getMessage());
        }
    }
    
    /**
     * Gets application status report
     *
     * @param programId Program ID to filter by (can be null for all programs)
     * @param periodId Period ID to filter by (can be null for all periods)
     * @return List of ApplicationStatusDTO objects
     * @throws IOException if communication error occurs
     */
    public List<ApplicationStatusDTO> getApplicationStatusReport(Long programId, Long periodId) throws IOException {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get application status report but no user is authenticated");
            throw new IOException("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get application status report");
            throw new IOException("Only administrators can access reports");
        }
        
        logger.debug("Getting application status report for programId: {}, periodId: {}", programId, periodId);
        
        try {
            // Create command
            GetApplicationStatusReportCommand command = new GetApplicationStatusReportCommand(programId, periodId);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_APPLICATION_STATUS_REPORT, command);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                ApplicationStatusReportResponse reportResponse = responseWrapper.getData();
                
                if (reportResponse != null) {
                    List<ApplicationStatusDTO> reportData = reportResponse.getReportData();
                    logger.debug("Received application status report with {} entries", reportData.size());
                    return reportData;
                } else {
                    logger.error("Received null report response");
                    throw new IOException("Error getting application status report: Null response");
                }
            } else {
                logger.error("Error getting application status report: {}", responseWrapper.getMessage());
                throw new IOException("Error getting application status report: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting application status report", e);
            throw new IOException("Error getting application status report: " + e.getMessage());
        }
    }
    
    /**
     * Gets user activity report
     *
     * @param startDate Start date for report period
     * @param endDate End date for report period
     * @return List of UserActivityDTO objects
     * @throws IOException if communication error occurs
     */
    public List<UserActivityDTO> getUserActivityReport(LocalDate startDate, LocalDate endDate) throws IOException {
        if (!isAuthenticated()) {
            logger.warn("Attempted to get user activity report but no user is authenticated");
            throw new IOException("User not authenticated");
        }
        
        if (!currentUser.getRole().equals(UserRole.ADMIN.name())) {
            logger.warn("Non-admin user attempted to get user activity report");
            throw new IOException("Only administrators can access reports");
        }
        
        logger.debug("Getting user activity report from {} to {}", startDate, endDate);
        
        try {
            // Create command
            GetUserActivityReportCommand command = new GetUserActivityReportCommand(startDate, endDate);
            
            // Create command wrapper
            CommandWrapper commandWrapper = new CommandWrapper(Command.GET_USER_ACTIVITY_REPORT, command);
            commandWrapper.setAuthToken(authToken);
            
            // Send command to server
            sendObject(commandWrapper);
            
            // Receive response
            ResponseWrapper responseWrapper = receiveObject();
            
            if (responseWrapper.getResponse() == ResponseFromServer.SUCCESS) {
                UserActivityReportResponse reportResponse = responseWrapper.getData();
                
                if (reportResponse != null) {
                    List<UserActivityDTO> reportData = reportResponse.getReportData();
                    logger.debug("Received user activity report with {} entries", reportData.size());
                    return reportData;
                } else {
                    logger.error("Received null report response");
                    throw new IOException("Error getting user activity report: Null response");
                }
            } else {
                logger.error("Error getting user activity report: {}", responseWrapper.getMessage());
                throw new IOException("Error getting user activity report: " + responseWrapper.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error getting user activity report", e);
            throw new IOException("Error getting user activity report: " + e.getMessage());
        }
    }
}
