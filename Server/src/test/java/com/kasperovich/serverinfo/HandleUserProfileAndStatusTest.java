package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.UpdateProfileResponse;
import com.kasperovich.commands.fromserver.UpdateUserStatusResponse;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.UpdateProfileCommand;
import com.kasperovich.commands.toserver.UpdateUserStatusCommand;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the user profile and status update logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleUserProfileAndStatusTest {

    private TestUserHandler userHandler;
    private TestUserService userService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;
    private boolean isAdmin = false;

    @BeforeEach
    void setUp() {
        userService = new TestUserService();
        userHandler = new TestUserHandler(userService, AUTHENTICATED_USER_ID);
        isAdmin = false;
    }

    /**
     * Test implementation of UserService
     */
    private class TestUserService extends UserService {
        private boolean throwIllegalArgumentException = false;
        private boolean throwGenericException = false;
        private String exceptionMessage = "";
        private UserDTO returnValue = null;
        private User mockUser = null;
        
        public void setThrowIllegalArgumentException(boolean throwException, String message) {
            this.throwIllegalArgumentException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setThrowGenericException(boolean throwException, String message) {
            this.throwGenericException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(UserDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        public void setMockUser(User user) {
            this.mockUser = user;
        }
        
        @Override
        public UserDTO updateUserProfile(Long userId, String username, String firstName, String lastName, String email) {
            if (throwIllegalArgumentException) {
                throw new IllegalArgumentException(exceptionMessage);
            }
            if (throwGenericException) {
                throw new RuntimeException(exceptionMessage);
            }
            return returnValue;
        }
        
        @Override
        public UserDTO updateUserStatus(Long userId, boolean active) {
            if (throwIllegalArgumentException) {
                throw new IllegalArgumentException(exceptionMessage);
            }
            if (throwGenericException) {
                throw new RuntimeException(exceptionMessage);
            }
            return returnValue;
        }
        
        @Override
        public User getUserById(Long userId) {
            return mockUser;
        }
    }
    
    /**
     * A simplified handler class that implements just the user update logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestUserHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final UserService userService;
        private Long authenticatedUserId;
        
        public TestUserHandler(UserService service, Long userId) {
            this.userService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles updating user profile
         */
        public void handleUpdateUserProfile(CommandWrapper commandWrapper) {
            try {
                // Extract profile update data
                UpdateProfileCommand command = commandWrapper.getData();

                if (command == null) {
                    UpdateProfileResponse response = new UpdateProfileResponse(
                            false,
                            "Profile update data is missing",
                            null
                    );
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                    return;
                }

                // Determine which user ID to update
                Long userIdToUpdate = authenticatedUserId;
                
                // If a specific user ID is provided and the authenticated user is an admin, use that ID
                if (command.getUserId() != null) {
                    User currentUser = userService.getUserById(authenticatedUserId);
                    if (currentUser != null && currentUser.getRole().equals(UserRole.ADMIN)) {
                        userIdToUpdate = command.getUserId();
                    } else {
                        UpdateProfileResponse response = new UpdateProfileResponse(
                                false,
                                "You do not have permission to update another user's profile",
                                null
                        );
                        sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                        return;
                    }
                }

                // Update the user profile
                UserDTO updatedUserDTO = userService.updateUserProfile(
                        userIdToUpdate,
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
                    sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
                } else {
                    UpdateProfileResponse response = new UpdateProfileResponse(
                            false,
                            "Failed to update user profile",
                            null
                    );
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                }
            } catch (IllegalArgumentException e) {
                // Handle specific errors like username already exists
                UpdateProfileResponse response = new UpdateProfileResponse(
                        false,
                        e.getMessage(),
                        null
                );
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
            } catch (Exception e) {
                UpdateProfileResponse response = new UpdateProfileResponse(
                        false,
                        "Error updating profile: " + e.getMessage(),
                        null
                );
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
            }
        }
        
        /**
         * Handles updating user status
         */
        public void handleUpdateUserStatus(CommandWrapper commandWrapper) {
            try {
                // Validate user is authenticated
                if (authenticatedUserId == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                    return;
                }

                UpdateUserStatusCommand command = commandWrapper.getData();

                if (command == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Command data is missing"));
                    return;
                }
                
                // Don't allow admins to deactivate themselves
                if (command.getUserId().equals(authenticatedUserId) && !command.isActive()) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "You cannot deactivate your own account"));
                    return;
                }

                // Update user status
                UserDTO updatedUser = userService.updateUserStatus(command.getUserId(), command.isActive());

                // Send response
                if (updatedUser != null) {
                    UpdateUserStatusResponse response = new UpdateUserStatusResponse(
                        true, 
                        "User status updated successfully", 
                        updatedUser
                    );
                    sendResponse(new ResponseWrapper(ResponseFromServer.USER_STATUS_UPDATED, response));
                } else {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Failed to update user status"));
                }
            } catch (Exception e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Error updating user status: " + e.getMessage()));
            }
        }
        
        private void sendResponse(ResponseWrapper response) {
            sentResponses.add(response);
        }
        
        public List<ResponseWrapper> getSentResponses() {
            return sentResponses;
        }
        
        public ResponseWrapper getLastSentResponse() {
            if (sentResponses.isEmpty()) {
                return null;
            }
            return sentResponses.get(sentResponses.size() - 1);
        }
    }

    /**
     * Creates a sample user DTO for testing
     */
    private UserDTO createSampleUserDTO(Long userId, String username, String firstName, String lastName, String email) {
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole("USER");
        return user;
    }
    
    /**
     * Creates a sample admin user for testing
     */
    private User createAdminUser() {
        User admin = new User();
        admin.setId(AUTHENTICATED_USER_ID);
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);
        return admin;
    }
    
    /**
     * Creates a sample regular user for testing
     */
    private User createRegularUser() {
        User user = new User();
        user.setId(AUTHENTICATED_USER_ID);
        user.setUsername("user");
        user.setRole(UserRole.STUDENT);
        return user;
    }

    // Tests for handleUpdateUserProfile
    
    @Test
    void testHandleUpdateUserProfile_Success() {
        // Arrange
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUsername("newusername");
        command.setFirstName("New");
        command.setLastName("User");
        command.setEmail("new.user@example.com");
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, command);
        
        // Create a sample user DTO for the response
        UserDTO updatedUser = createSampleUserDTO(
            AUTHENTICATED_USER_ID, 
            "newusername", 
            "New", 
            "User", 
            "new.user@example.com"
        );
        
        // Configure the service to return the user DTO
        userService.setReturnValue(updatedUser);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserProfile(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        UpdateProfileResponse profileResponse = (UpdateProfileResponse) response.getData();
        assertTrue(profileResponse.isSuccess());
        assertEquals("Profile updated successfully", profileResponse.getMessage());
        assertEquals(updatedUser, profileResponse.getUser());
    }
    
    @Test
    void testHandleUpdateUserProfile_NullCommand() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, null);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserProfile(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        UpdateProfileResponse profileResponse = (UpdateProfileResponse) response.getData();
        assertFalse(profileResponse.isSuccess());
        assertEquals("Profile update data is missing", profileResponse.getMessage());
        assertNull(profileResponse.getUser());
    }
    
    @Test
    void testHandleUpdateUserProfile_AdminUpdatingOtherUser_Success() {
        // Arrange
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(OTHER_USER_ID);
        command.setUsername("otheruser");
        command.setFirstName("Other");
        command.setLastName("User");
        command.setEmail("other.user@example.com");
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, command);
        
        // Create a sample admin user
        User adminUser = createAdminUser();
        userService.setMockUser(adminUser);
        
        // Create a sample user DTO for the response
        UserDTO updatedUser = createSampleUserDTO(
            OTHER_USER_ID, 
            "otheruser", 
            "Other", 
            "User", 
            "other.user@example.com"
        );
        
        // Configure the service to return the user DTO
        userService.setReturnValue(updatedUser);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserProfile(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        UpdateProfileResponse profileResponse = (UpdateProfileResponse) response.getData();
        assertTrue(profileResponse.isSuccess());
        assertEquals("Profile updated successfully", profileResponse.getMessage());
        assertEquals(updatedUser, profileResponse.getUser());
    }
    
    @Test
    void testHandleUpdateUserProfile_NonAdminUpdatingOtherUser_Failure() {
        // Arrange
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(OTHER_USER_ID);
        command.setUsername("otheruser");
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, command);
        
        // Create a sample regular user
        User regularUser = createRegularUser();
        userService.setMockUser(regularUser);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserProfile(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        UpdateProfileResponse profileResponse = (UpdateProfileResponse) response.getData();
        assertFalse(profileResponse.isSuccess());
        assertEquals("You do not have permission to update another user's profile", profileResponse.getMessage());
        assertNull(profileResponse.getUser());
    }
    
    @Test
    void testHandleUpdateUserProfile_ValidationError() {
        // Arrange
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUsername("existingusername");
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_PROFILE, command);
        
        // Configure the service to throw an IllegalArgumentException
        String errorMessage = "Username already exists";
        userService.setThrowIllegalArgumentException(true, errorMessage);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserProfile(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        UpdateProfileResponse profileResponse = (UpdateProfileResponse) response.getData();
        assertFalse(profileResponse.isSuccess());
        assertEquals(errorMessage, profileResponse.getMessage());
        assertNull(profileResponse.getUser());
    }
    
    // Tests for handleUpdateUserStatus
    
    @Test
    void testHandleUpdateUserStatus_Success() {
        // Arrange
        UpdateUserStatusCommand command = new UpdateUserStatusCommand();
        command.setUserId(OTHER_USER_ID);
        command.setActive(false);
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_USER_STATUS, command);
        
        // Create a sample user DTO for the response
        UserDTO updatedUser = createSampleUserDTO(
            OTHER_USER_ID, 
            "otheruser", 
            "Other", 
            "User", 
            "other.user@example.com"
        );
        
        // Configure the service to return the user DTO
        userService.setReturnValue(updatedUser);
        
        // Act - Call the method under test
        userHandler.handleUpdateUserStatus(commandWrapper);
        
        // Assert
        ResponseWrapper response = userHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.USER_STATUS_UPDATED, response.getResponse());
        
        UpdateUserStatusResponse statusResponse = (UpdateUserStatusResponse) response.getData();
        assertTrue(statusResponse.isSuccess());
        assertEquals("User status updated successfully", statusResponse.getMessage());
        assertEquals(updatedUser, statusResponse.getUser());
    }

}
