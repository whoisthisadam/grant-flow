package com.kasperovich.clientconnection;

import com.kasperovich.commands.fromserver.*;
import com.kasperovich.commands.toserver.*;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.utils.LoggerUtil;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientConnection {
    private static final Logger logger = LoggerUtil.getLogger(ClientConnection.class);
    private static final int DEFAULT_TIMEOUT_MS = 3000;
    
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
     * @throws IOException if an I/O error occurs during communication
     * @throws ClassNotFoundException if the class of the received object cannot be found
     * @throws Exception if the server returns an error message
     */
    public ScholarshipApplicationDTO submitScholarshipApplication(Long programId, Long periodId, String additionalInfo) throws IOException, ClassNotFoundException, Exception {
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
        if (!currentUser.getRole().equals("ADMIN")) {
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
        if (!currentUser.getRole().equals("ADMIN")) {
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
        if (!currentUser.getRole().equals("ADMIN")) {
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
}
