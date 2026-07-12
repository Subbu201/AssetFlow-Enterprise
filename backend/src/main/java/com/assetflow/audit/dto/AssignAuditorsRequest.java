package com.assetflow.audit.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssignAuditorsRequest {

    @NotEmpty
    private List<Long> auditorUserIds;
}
