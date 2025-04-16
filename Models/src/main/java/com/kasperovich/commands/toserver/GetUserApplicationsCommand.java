package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data object for requesting a user's scholarship applications from the server.
 * To be used with CommandWrapper and Command.GET_MY_APPLICATIONS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserApplicationsCommand implements Serializable {
    
    private Long userId;
    private String status;
    
    public GetUserApplicationsCommand(Long userId) {
        this.userId = userId;
    }
}
