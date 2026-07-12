package com.assetflow.assetreturn.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {
    @NotNull
    private Long allocationId;

    @NotNull
    private Long assetId;

    @NotNull
    private Long requestedByUserId;

    private LocalDateTime requestedAt;
    private String returnCondition;
    private String checkInNotes;
}
