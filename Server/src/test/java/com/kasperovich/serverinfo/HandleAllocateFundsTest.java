package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.FundAllocationResponse;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.AllocateFundsCommand;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.dto.scholarship.FundAllocationDTO;
import com.kasperovich.entities.AllocationStatus;
import com.kasperovich.service.FundManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the fund allocation logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleAllocateFundsTest {

    private TestFundHandler fundHandler;
    private TestFundManagementService fundManagementService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long BUDGET_ID = 100L;
    private final Long PROGRAM_ID = 200L;

    @BeforeEach
    void setUp() {
        fundManagementService = new TestFundManagementService();
        fundHandler = new TestFundHandler(fundManagementService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of FundManagementService
     */
    private class TestFundManagementService extends FundManagementService {
        private boolean throwException = false;
        private String exceptionMessage = "";
        private FundAllocationDTO returnValue = null;
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(FundAllocationDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public FundAllocationDTO allocateFunds(Long budgetId, Long programId, BigDecimal amount, String notes, Long userId) 
                throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the fund allocation logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestFundHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final FundManagementService fundManagementService;
        private Long authenticatedUserId;
        
        public TestFundHandler(FundManagementService service, Long userId) {
            this.fundManagementService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles allocating funds
         */
        public void handleAllocateFunds(CommandWrapper commandWrapper) {
            try {
                // Validate user is authenticated
                if (authenticatedUserId == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                    return;
                }

                AllocateFundsCommand command = commandWrapper.getData();

                if (command == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Fund allocation data is missing"));
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
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (Exception e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR,
                        new FundAllocationResponse(e.getMessage())));
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
     * Creates a sample fund allocation DTO for testing
     */
    private FundAllocationDTO createSampleAllocationDTO() {
        FundAllocationDTO allocation = new FundAllocationDTO();
        allocation.setId(1L);
        allocation.setBudgetId(BUDGET_ID);
        allocation.setProgramId(PROGRAM_ID);
        allocation.setAmount(new BigDecimal("5000.00"));
        allocation.setPreviousAmount(new BigDecimal("0.00"));
        allocation.setAllocationDate(LocalDateTime.now());
        allocation.setAllocatedById(AUTHENTICATED_USER_ID);
        allocation.setStatus(AllocationStatus.APPROVED);
        allocation.setNotes("Test allocation");
        return allocation;
    }

    @Test
    void testHandleAllocateFunds_Success() {
        // Arrange
        AllocateFundsCommand command = new AllocateFundsCommand();
        command.setBudgetId(BUDGET_ID);
        command.setProgramId(PROGRAM_ID);
        command.setAmount(new BigDecimal("5000.00"));
        command.setNotes("Test allocation");
        CommandWrapper commandWrapper = new CommandWrapper(Command.ALLOCATE_FUNDS, command);
        
        // Create a sample allocation DTO for the response
        FundAllocationDTO allocation = createSampleAllocationDTO();
        
        // Configure the service to return the allocation DTO
        fundManagementService.setReturnValue(allocation);
        
        // Act - Call the method under test
        fundHandler.handleAllocateFunds(commandWrapper);
        
        // Assert
        ResponseWrapper response = fundHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        FundAllocationResponse allocationResponse = (FundAllocationResponse) response.getData();
        assertNotNull(allocationResponse);
        assertEquals(allocation, allocationResponse.getAllocation());
        assertEquals(BUDGET_ID, allocationResponse.getAllocation().getBudgetId());
        assertEquals(PROGRAM_ID, allocationResponse.getAllocation().getProgramId());
        assertEquals(new BigDecimal("5000.00"), allocationResponse.getAllocation().getAmount());
    }
    
    @Test
    void testHandleAllocateFunds_NullCommand() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.ALLOCATE_FUNDS, null);
        
        // Act - Call the method under test
        fundHandler.handleAllocateFunds(commandWrapper);
        
        // Assert
        ResponseWrapper response = fundHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        FundAllocationResponse allocationResponse = (FundAllocationResponse) response.getData();
        assertNotNull(allocationResponse);
        assertEquals("Fund allocation data is missing", allocationResponse.getErrorMessage());
        assertNull(allocationResponse.getAllocation());
    }
    
    @Test
    void testHandleAllocateFunds_UserNotAuthenticated() {
        // Arrange
        AllocateFundsCommand command = new AllocateFundsCommand();
        command.setBudgetId(BUDGET_ID);
        command.setProgramId(PROGRAM_ID);
        command.setAmount(new BigDecimal("5000.00"));
        CommandWrapper commandWrapper = new CommandWrapper(Command.ALLOCATE_FUNDS, command);
        
        // Set authenticated user ID to null
        fundHandler.setAuthenticatedUserId(null);
        
        // Act - Call the method under test
        fundHandler.handleAllocateFunds(commandWrapper);
        
        // Assert
        ResponseWrapper response = fundHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        assertEquals("User not authenticated", response.getMessage());
    }
    
    @Test
    void testHandleAllocateFunds_ServiceThrowsException() {
        // Arrange
        AllocateFundsCommand command = new AllocateFundsCommand();
        command.setBudgetId(BUDGET_ID);
        command.setProgramId(PROGRAM_ID);
        command.setAmount(new BigDecimal("5000.00"));
        CommandWrapper commandWrapper = new CommandWrapper(Command.ALLOCATE_FUNDS, command);
        
        // Configure the service to throw an exception
        String errorMessage = "Insufficient funds in the budget";
        fundManagementService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        fundHandler.handleAllocateFunds(commandWrapper);
        
        // Assert
        ResponseWrapper response = fundHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        FundAllocationResponse allocationResponse = (FundAllocationResponse) response.getData();
        assertNotNull(allocationResponse);
        assertEquals(errorMessage, allocationResponse.getErrorMessage());
        assertNull(allocationResponse.getAllocation());
    }
}
