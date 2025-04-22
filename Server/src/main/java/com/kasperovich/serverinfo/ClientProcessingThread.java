package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.*;
import com.kasperovich.commands.toserver.*;
import com.kasperovich.config.ConnectedClientConfig;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.*;
import com.kasperovich.service.*;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
    private final FundManagementService fundManagementService;
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
        this.fundManagementService = new FundManagementService();
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
            case CREATE_SCHOLARSHIP_PROGRAM: {
                handleCreateScholarshipProgram(commandWrapper);
                break;
            }
            case UPDATE_SCHOLARSHIP_PROGRAM: {
                handleUpdateScholarshipProgram(commandWrapper);
                break;
            }
            case DELETE_SCHOLARSHIP_PROGRAM: {
                handleDeleteScholarshipProgram(commandWrapper);
                break;
            }
            case GET_PENDING_APPLICATIONS: {
                handleGetPendingApplications(commandWrapper);
                break;
            }
            case GET_ALL_APPLICATIONS: {
                handleGetAllApplications(commandWrapper);
                break;
            }
            case APPROVE_APPLICATION: {
                handleApproveApplication(commandWrapper);
                break;
            }
            case REJECT_APPLICATION: {
                handleRejectApplication(commandWrapper);
                break;
            }
            case GET_ALL_BUDGETS: {
                handleGetAllBudgets(commandWrapper);
                break;
            }
            case GET_ACTIVE_BUDGET: {
                handleGetActiveBudget(commandWrapper);
                break;
            }
            case CREATE_BUDGET: {
                handleCreateBudget(commandWrapper);
                break;
            }
            case UPDATE_BUDGET: {
                handleUpdateBudget(commandWrapper);
                break;
            }
            case ACTIVATE_BUDGET: {
                handleActivateBudget(commandWrapper);
                break;
            }
            case CLOSE_BUDGET: {
                handleCloseBudget(commandWrapper);
                break;
            }
            case ALLOCATE_FUNDS: {
                handleAllocateFunds(commandWrapper);
                break;
            }
            case GET_ALLOCATIONS_BY_BUDGET: {
                handleGetAllocationsByBudget(commandWrapper);
                break;
            }
            case GET_ALLOCATIONS_BY_PROGRAM: {
                handleGetAllocationsByProgram(commandWrapper);
                break;
            }
            case CREATE_ACADEMIC_PERIOD:
                handleCreateAcademicPeriod(commandWrapper);
                break;
            case UPDATE_ACADEMIC_PERIOD:
                handleUpdateAcademicPeriod(commandWrapper);
                break;
            case UPDATE_ACADEMIC_PERIOD_STATUS:
                handleUpdateAcademicPeriodStatus(commandWrapper);
                break;
            case DELETE_ACADEMIC_PERIOD:
                handleDeleteAcademicPeriod(commandWrapper);
                break;
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
     * Handles creating a scholarship program.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleCreateScholarshipProgram(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling CREATE_SCHOLARSHIP_PROGRAM command");

        try {
            // Extract program data
            CreateScholarshipProgramCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Scholarship program data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Scholarship program data is missing"));
                return;
            }

            // Create the scholarship program
            ScholarshipProgramDTO program = scholarshipService.createScholarshipProgram(command, authenticatedUserId);

            // Create response with the program
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.success(
                    "Scholarship program created successfully",
                    program,
                    ScholarshipProgramOperationResponse.OperationType.CREATE
            );

            logger.info("Scholarship program created successfully. ID: {}", program.getId());
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating scholarship program: {}", e.getMessage());
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.CREATE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        } catch (Exception e) {
            logger.error("Error creating scholarship program", e);
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    "Error creating scholarship program: " + e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.CREATE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        }
    }

    /**
     * Handles updating a scholarship program.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleUpdateScholarshipProgram(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling UPDATE_SCHOLARSHIP_PROGRAM command");

        try {
            // Extract program data
            UpdateScholarshipProgramCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Scholarship program data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Scholarship program data is missing"));
                return;
            }

            // Update the scholarship program
            ScholarshipProgramDTO program = scholarshipService.updateScholarshipProgram(command, authenticatedUserId);

            // Create response with the program
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.success(
                    "Scholarship program updated successfully",
                    program,
                    ScholarshipProgramOperationResponse.OperationType.UPDATE
            );

            logger.info("Scholarship program updated successfully. ID: {}", program.getId());
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating scholarship program: {}", e.getMessage());
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.UPDATE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        } catch (Exception e) {
            logger.error("Error updating scholarship program", e);
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    "Error updating scholarship program: " + e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.UPDATE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        }
    }

    /**
     * Handles deleting a scholarship program.
     *
     * @param commandWrapper the command wrapper
     * @throws IOException if an I/O error occurs
     */
    private void handleDeleteScholarshipProgram(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling DELETE_SCHOLARSHIP_PROGRAM command");

        try {
            // Extract program ID
            Long programId = commandWrapper.getData();

            if (programId == null) {
                logger.warn("Scholarship program ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Scholarship program ID is missing"));
                return;
            }

            // Delete the scholarship program
            boolean deleted = scholarshipService.deleteScholarshipProgram(programId, authenticatedUserId);

            // Create response
            ScholarshipProgramOperationResponse response = deleted ?
                    ScholarshipProgramOperationResponse.success(
                            "Scholarship program deleted successfully",
                            null,
                            ScholarshipProgramOperationResponse.OperationType.DELETE
                    ) :
                    ScholarshipProgramOperationResponse.error(
                            "Failed to delete scholarship program",
                            ScholarshipProgramOperationResponse.OperationType.DELETE
                    );

            logger.info("Scholarship program deletion result for ID {}: {}", programId, deleted);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting scholarship program: {}", e.getMessage());
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.DELETE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        } catch (Exception e) {
            logger.error("Error deleting scholarship program", e);
            ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                    "Error deleting scholarship program: " + e.getMessage(),
                    ScholarshipProgramOperationResponse.OperationType.DELETE
            );
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
        }
    }

    /**
     * Handles the GET_PENDING_APPLICATIONS command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetPendingApplications(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_PENDING_APPLICATIONS command");

        try {
            // Get pending applications for admin
            List<ScholarshipApplicationDTO> applications = scholarshipApplicationService.getPendingApplicationsForAdmin(authenticatedUserId);

            // Send response
            ApplicationsResponse response = new ApplicationsResponse(applications);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent {} pending applications to user: {}", applications.size(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_PENDING_APPLICATIONS command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new ApplicationsResponse(e.getMessage())));
        }
    }

    /**
     * Handles the GET_ALL_APPLICATIONS command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetAllApplications(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ALL_APPLICATIONS command");

        try {
            // Get all applications for admin
            List<ScholarshipApplicationDTO> applications = scholarshipApplicationService.getAllApplicationsForAdmin(authenticatedUserId);

            // Send response
            ApplicationsResponse response = new ApplicationsResponse(applications);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent {} applications to user: {}", applications.size(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_ALL_APPLICATIONS command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new ApplicationsResponse(e.getMessage())));
        }
    }

    /**
     * Handles the APPROVE_APPLICATION command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleApproveApplication(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling APPROVE_APPLICATION command");

        try {
            ApproveApplicationCommand command = commandWrapper.getData();
            String comments = command.getComments();

            // Approve the application with auth validation
            ScholarshipApplicationDTO application = scholarshipApplicationService.approveApplicationWithAuth(
                    command.getApplicationId(), authenticatedUserId, comments);

            // Send response
            ApplicationReviewResponse response = new ApplicationReviewResponse(application);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Application {} approved by user: {}", command.getApplicationId(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling APPROVE_APPLICATION command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new ApplicationReviewResponse(e.getMessage())));
        }
    }

    /**
     * Handles the REJECT_APPLICATION command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleRejectApplication(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling REJECT_APPLICATION command");

        try {
            RejectApplicationCommand command = commandWrapper.getData();
            String comments = command.getComments();

            // Reject the application with auth validation
            ScholarshipApplicationDTO application = scholarshipApplicationService.rejectApplicationWithAuth(
                    command.getApplicationId(), authenticatedUserId, comments);

            // Send response
            ApplicationReviewResponse response = new ApplicationReviewResponse(application);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Application {} rejected by user: {}", command.getApplicationId(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling REJECT_APPLICATION command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new ApplicationReviewResponse(e.getMessage())));
        }
    }

    /**
     * Handles the GET_ALL_BUDGETS command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetAllBudgets(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ALL_BUDGETS command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            // Get all budgets
            List<BudgetDTO> budgets = fundManagementService.getAllBudgets();

            // Send response
            BudgetsResponse response = new BudgetsResponse(budgets);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent {} budgets to user: {}", budgets.size(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_ALL_BUDGETS command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetsResponse(e.getMessage())));
        }
    }

    /**
     * Handles the GET_ACTIVE_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetActiveBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ACTIVE_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            // Get active budget
            BudgetDTO budget = fundManagementService.getActiveBudget();

            // Send response
            BudgetResponse response = new BudgetResponse(budget);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent active budget to user: {}", authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_ACTIVE_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetResponse(e.getMessage())));
        }
    }

    /**
     * Handles the CREATE_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleCreateBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling CREATE_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            CreateBudgetCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Budget data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Budget data is missing"));
                return;
            }

            // Create budget
            BudgetDTO budget = fundManagementService.createBudget(
                    command.getFiscalYear(),
                    command.getFiscalPeriod(),
                    command.getTotalAmount(),
                    command.getStartDate(),
                    command.getEndDate(),
                    command.getDescription(),
                    authenticatedUserId
            );

            // Send response
            BudgetResponse response = new BudgetResponse(budget);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Budget created successfully. ID: {}", budget.getId());

        } catch (Exception e) {
            logger.error("Error handling CREATE_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetResponse(e.getMessage())));
        }
    }

    /**
     * Handles the UPDATE_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleUpdateBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling UPDATE_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            UpdateBudgetCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Budget data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Budget data is missing"));
                return;
            }

            // Update budget
            BudgetDTO budget = fundManagementService.updateBudget(
                    command.getId(),
                    command.getFiscalYear(),
                    command.getFiscalPeriod(),
                    command.getTotalAmount(),
                    command.getStartDate(),
                    command.getEndDate(),
                    command.getDescription(),
                    command.getStatus(),
                    authenticatedUserId
            );

            // Send response
            BudgetResponse response = new BudgetResponse(budget);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Budget updated successfully. ID: {}", budget.getId());

        } catch (Exception e) {
            logger.error("Error handling UPDATE_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetResponse(e.getMessage())));
        }
    }

    /**
     * Handles the ACTIVATE_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleActivateBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling ACTIVATE_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            ActivateBudgetCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Budget ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Budget ID is missing"));
                return;
            }

            // Activate budget
            BudgetDTO budget = fundManagementService.activateBudget(command.getBudgetId(), authenticatedUserId);

            // Send response
            BudgetResponse response = new BudgetResponse(budget);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Budget activated successfully. ID: {}", budget.getId());

        } catch (Exception e) {
            logger.error("Error handling ACTIVATE_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetResponse(e.getMessage())));
        }
    }

    /**
     * Handles the CLOSE_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleCloseBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling CLOSE_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            CloseBudgetCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Budget ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Budget ID is missing"));
                return;
            }

            // Close budget
            BudgetDTO budget = fundManagementService.closeBudget(command.getBudgetId(), authenticatedUserId);

            // Send response
            BudgetResponse response = new BudgetResponse(budget);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Budget closed successfully. ID: {}", budget.getId());

        } catch (Exception e) {
            logger.error("Error handling CLOSE_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new BudgetResponse(e.getMessage())));
        }
    }

    /**
     * Handles the ALLOCATE_FUNDS command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleAllocateFunds(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling ALLOCATE_FUNDS command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            AllocateFundsCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Fund allocation data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Fund allocation data is missing"));
                return;
            }

            // Allocate funds
            FundAllocationDTO allocation = fundManagementService.allocateFunds(
                    command.getBudgetId(),
                    command.getProgramId(),
                    command.getAmount(),
                    command.getNotes(),
                    authenticatedUserId
            );

            // Send response
            FundAllocationResponse response = new FundAllocationResponse(allocation);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Funds allocated successfully. ID: {}", allocation.getId());

        } catch (Exception e) {
            logger.error("Error handling ALLOCATE_FUNDS command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new FundAllocationResponse(e.getMessage())));
        }
    }

    /**
     * Handles the GET_ALLOCATIONS_BY_BUDGET command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetAllocationsByBudget(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ALLOCATIONS_BY_BUDGET command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            GetAllocationsByBudgetCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Budget ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Budget ID is missing"));
                return;
            }

            // Get allocations by budget
            List<FundAllocationDTO> allocations = fundManagementService.getAllocationsByBudget(command.getBudgetId());

            // Send response
            FundAllocationsResponse response = new FundAllocationsResponse(allocations);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent {} allocations to user: {}", allocations.size(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_ALLOCATIONS_BY_BUDGET command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new FundAllocationsResponse(e.getMessage())));
        }
    }

    /**
     * Handles the GET_ALLOCATIONS_BY_PROGRAM command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleGetAllocationsByProgram(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling GET_ALLOCATIONS_BY_PROGRAM command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            GetAllocationsByProgramCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Program ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Program ID is missing"));
                return;
            }

            // Get allocations by program
            List<FundAllocationDTO> allocations = fundManagementService.getAllocationsByProgram(command.getProgramId());

            // Send response
            FundAllocationsResponse response = new FundAllocationsResponse(allocations);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Sent {} allocations to user: {}", allocations.size(), authenticatedUserId);

        } catch (Exception e) {
            logger.error("Error handling GET_ALLOCATIONS_BY_PROGRAM command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR,
                    new FundAllocationsResponse(e.getMessage())));
        }
    }

    /**
     * Handles the CREATE_ACADEMIC_PERIOD command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleCreateAcademicPeriod(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling CREATE_ACADEMIC_PERIOD command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            CreateAcademicPeriodCommand command = commandWrapper.getData();

            if (command == null || command.getPeriod() == null) {
                logger.warn("Academic period data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Academic period data is missing"));
                return;
            }

            // Create academic period
            AcademicPeriodDTO period = academicPeriodService.createAcademicPeriod(command.getPeriod());

            // Send response
            AcademicPeriodResponse response = new AcademicPeriodResponse(period);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Academic period created successfully. ID: {}", period.getId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid academic period data: {}", e.getMessage());
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error handling CREATE_ACADEMIC_PERIOD command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error creating academic period"));
        }
    }

    /**
     * Handles the UPDATE_ACADEMIC_PERIOD command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleUpdateAcademicPeriod(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling UPDATE_ACADEMIC_PERIOD command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            UpdateAcademicPeriodCommand command = commandWrapper.getData();

            if (command == null || command.getPeriod() == null) {
                logger.warn("Academic period data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Academic period data is missing"));
                return;
            }

            // Update academic period
            AcademicPeriodDTO period = academicPeriodService.updateAcademicPeriod(command.getPeriod());

            // Send response
            AcademicPeriodResponse response = new AcademicPeriodResponse(period);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Academic period updated successfully. ID: {}", period.getId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid academic period data: {}", e.getMessage());
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error handling UPDATE_ACADEMIC_PERIOD command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error updating academic period"));
        }
    }

    /**
     * Handles the UPDATE_ACADEMIC_PERIOD_STATUS command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleUpdateAcademicPeriodStatus(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling UPDATE_ACADEMIC_PERIOD_STATUS command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            UpdateAcademicPeriodStatusCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Academic period status data is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Academic period status data is missing"));
                return;
            }

            // Update academic period status
            AcademicPeriodDTO period = academicPeriodService.updateAcademicPeriodStatus(
                    command.getPeriodId(), command.isActive());

            // Send response
            AcademicPeriodResponse response = new AcademicPeriodResponse(period);
            sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            logger.info("Academic period status updated successfully. ID: {}", period.getId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid academic period status data: {}", e.getMessage());
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error handling UPDATE_ACADEMIC_PERIOD_STATUS command", e);
            sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Error updating academic period status"));
        }
    }

    /**
     * Handles the DELETE_ACADEMIC_PERIOD command.
     *
     * @param commandWrapper the command wrapper
     */
    private void handleDeleteAcademicPeriod(CommandWrapper commandWrapper) throws IOException {
        logger.debug("Handling DELETE_ACADEMIC_PERIOD command");

        try {
            // Validate user is admin
            if (authenticatedUserId == null) {
                logger.warn("User not authenticated");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                return;
            }

            DeleteAcademicPeriodCommand command = commandWrapper.getData();

            if (command == null) {
                logger.warn("Academic period ID is missing");
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Academic period ID is missing"));
                return;
            }

            // Delete academic period
            boolean deleted = academicPeriodService.deleteAcademicPeriod(command.getPeriodId());

            // Send response
            if (deleted) {
                sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, "Academic period deleted successfully"));
                logger.info("Academic period deleted successfully. ID: {}", command.getPeriodId());
            } else {
                sendObject(new ResponseWrapper(ResponseFromServer.ERROR, "Failed to delete academic period"));
                logger.warn("Failed to delete academic period. ID: {}", command.getPeriodId());
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid academic period ID: {}", e.getMessage());
            var response = new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage());
            response.setMessage(e.getMessage());
            sendObject(response);
        } catch (Exception e) {
            logger.error("Error handling DELETE_ACADEMIC_PERIOD command", e);
            var response = new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage());
            response.setMessage("Error deleting academic period");
            sendObject(response);
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
