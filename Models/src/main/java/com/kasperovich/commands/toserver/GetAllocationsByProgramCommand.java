package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command for getting fund allocations by program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllocationsByProgramCommand implements Serializable {
    private Long programId;
}
