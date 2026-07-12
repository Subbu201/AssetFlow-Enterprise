package com.assetflow.organization.employee.entity;

import com.assetflow.common.BaseEntity;
import com.assetflow.common.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "employee_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfile extends BaseEntity {

    @Column(name = "user_account_id", nullable = false, unique = true)
    private Long userAccountId;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(nullable = false)
    private String designation;

    private String phone;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;

}
