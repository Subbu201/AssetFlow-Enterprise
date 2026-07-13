package com.assetflow.auth.service;

import com.assetflow.auth.entity.UserAccount;
import com.assetflow.auth.repository.UserRepository;
import com.assetflow.common.RecordStatus;
import com.assetflow.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.assetflow.organization.department.repository.DepartmentRepository departmentRepository;
    private final com.assetflow.organization.category.repository.AssetCategoryRepository categoryRepository;
    private final com.assetflow.organization.employee.repository.EmployeeProfileRepository employeeRepository;
    private final com.assetflow.notification.NotificationRepository notificationRepository;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap.admin.email:admin@assetflow.com}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:Admin@123}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap.admin.name:AssetFlow Admin}")
    private String bootstrapAdminName;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdmin() {
        // 1. Correct corrupted Admin name if needed
        java.util.Optional<UserAccount> adminOpt = userRepository.findByEmailIgnoreCase(bootstrapAdminEmail.toLowerCase());
        UserAccount adminUser = null;
        if (adminOpt.isPresent()) {
            adminUser = adminOpt.get();
            if (adminUser.getFullName().contains("spring.mail.port")) {
                adminUser.setFullName("AssetFlow Admin");
                userRepository.save(adminUser);
                log.info("Corrected corrupted ADMIN full name in database.");
            }
        } else {
            // Bootstrap Admin
            if (bootstrapAdminEnabled) {
                UserAccount admin = UserAccount.builder()
                        .fullName(bootstrapAdminName)
                        .email(bootstrapAdminEmail.toLowerCase())
                        .password(passwordEncoder.encode(bootstrapAdminPassword))
                        .role(Role.ADMIN)
                        .status(RecordStatus.ACTIVE)
                        .build();
                adminUser = userRepository.save(admin);
                log.info("Successfully bootstrapped initial ADMIN account: {}", bootstrapAdminEmail);
            }
        }

        if (adminUser != null && !employeeRepository.existsByUserAccountId(adminUser.getId())) {
            com.assetflow.organization.employee.entity.EmployeeProfile profile = com.assetflow.organization.employee.entity.EmployeeProfile.builder()
                    .userAccountId(adminUser.getId())
                    .employeeCode("EMP-ADMIN")
                    .designation("Administrator")
                    .joiningDate(java.time.LocalDate.now())
                    .status(RecordStatus.ACTIVE)
                    .build();
            employeeRepository.save(profile);
            log.info("Created missing EmployeeProfile for ADMIN user.");
        }

        // 2. Bootstrap Departments
        Long deptId = null;
        if (departmentRepository.count() == 0) {
            com.assetflow.organization.department.entity.Department dept1 = com.assetflow.organization.department.entity.Department.builder()
                    .name("IT Support")
                    .code("ITS")
                    .description("Information Technology and support services")
                    .build();
            com.assetflow.organization.department.entity.Department dept2 = com.assetflow.organization.department.entity.Department.builder()
                    .name("Human Resources")
                    .code("HR")
                    .description("HR and Recruiting")
                    .build();
            deptId = departmentRepository.save(dept1).getId();
            departmentRepository.save(dept2);
            log.info("Bootstrapped IT Support and HR departments.");
        } else {
            deptId = departmentRepository.findAll().get(0).getId();
        }

        // 3. Bootstrap Categories
        if (categoryRepository.count() == 0) {
            categoryRepository.save(com.assetflow.organization.category.entity.AssetCategory.builder()
                    .name("Laptops")
                    .code("LAP")
                    .description("Company-provided laptop computers")
                    .warrantyPeriodMonths(36)
                    .build());
            categoryRepository.save(com.assetflow.organization.category.entity.AssetCategory.builder()
                    .name("Monitors")
                    .code("MON")
                    .description("Desktop monitors")
                    .warrantyPeriodMonths(24)
                    .build());
            categoryRepository.save(com.assetflow.organization.category.entity.AssetCategory.builder()
                    .name("Mobile Devices")
                    .code("MOB")
                    .description("Smartphones and tablets")
                    .warrantyPeriodMonths(12)
                    .build());
            log.info("Bootstrapped default asset categories.");
        }

        // 4. Bootstrap Dummy Employee
        // 4. Bootstrap Dummy Employee and self-heal missing EmployeeProfiles
        List<UserAccount> employees = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .toList();

        if (employees.isEmpty()) {
            UserAccount employeeUser = UserAccount.builder()
                    .fullName("Dummy Employee")
                    .email("employee@assetflow.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.EMPLOYEE)
                    .status(RecordStatus.ACTIVE)
                    .build();
            UserAccount savedUser = userRepository.save(employeeUser);

            com.assetflow.organization.employee.entity.EmployeeProfile profile = com.assetflow.organization.employee.entity.EmployeeProfile.builder()
                    .userAccountId(savedUser.getId())
                    .employeeCode("EMP-001")
                    .designation("Support Specialist")
                    .departmentId(deptId)
                    .joiningDate(java.time.LocalDate.now())
                    .status(RecordStatus.ACTIVE)
                    .build();
            employeeRepository.save(profile);
            log.info("Bootstrapped initial EMPLOYEE account: employee@assetflow.com");
        } else {
            // For any existing employee users that don't have a profile, create one
            for (UserAccount emp : employees) {
                if (!employeeRepository.existsByUserAccountId(emp.getId())) {
                    com.assetflow.organization.employee.entity.EmployeeProfile profile = com.assetflow.organization.employee.entity.EmployeeProfile.builder()
                            .userAccountId(emp.getId())
                            .employeeCode("EMP-" + String.format("%04d", emp.getId()))
                            .designation("Employee")
                            .departmentId(deptId)
                            .joiningDate(java.time.LocalDate.now())
                            .status(RecordStatus.ACTIVE)
                            .build();
                    employeeRepository.save(profile);
                    log.info("Self-healed missing EmployeeProfile for user: {}", emp.getEmail());
                }
            }
        }

        // 5. Bootstrap Notifications for Admin & Employee (non-critical - wrapped in try-catch)
        try {
            userRepository.findByEmailIgnoreCase(bootstrapAdminEmail.toLowerCase()).ifPresent(admin -> {
                if (notificationRepository.countByRecipientUserIdAndReadFalse(admin.getId()) == 0) {
                    com.assetflow.notification.Notification notif1 = new com.assetflow.notification.Notification();
                    notif1.setRecipientUserId(admin.getId());
                    notif1.setType(com.assetflow.notification.NotificationType.OVERDUE_RETURN);
                    notif1.setTitle("URGENT: Return Overdue");
                    notif1.setMessage("Critical: 3 asset allocations are overdue for return. Please review and send return reminders.");
                    notif1.setReferenceType("OVERDUE");
                    notif1.setReferenceId(1L);
                    notif1.setRead(false);
                    notificationRepository.save(notif1);

                    com.assetflow.notification.Notification notif2 = new com.assetflow.notification.Notification();
                    notif2.setRecipientUserId(admin.getId());
                    notif2.setType(com.assetflow.notification.NotificationType.ASSET_ASSIGNED);
                    notif2.setTitle("General Notice");
                    notif2.setMessage("Welcome to AssetFlow Enterprise! Please complete the category mappings for the new quarter.");
                    notif2.setReferenceType("SYSTEM");
                    notif2.setReferenceId(2L);
                    notif2.setRead(false);
                    notificationRepository.save(notif2);
                    log.info("Bootstrapped notifications for ADMIN.");
                }
            });

            userRepository.findByEmailIgnoreCase("employee@assetflow.com").ifPresent(emp -> {
                if (notificationRepository.countByRecipientUserIdAndReadFalse(emp.getId()) == 0) {
                    com.assetflow.notification.Notification notif1 = new com.assetflow.notification.Notification();
                    notif1.setRecipientUserId(emp.getId());
                    notif1.setType(com.assetflow.notification.NotificationType.BOOKING_REMINDER);
                    notif1.setTitle("URGENT: Warranty Warning");
                    notif1.setMessage("Your allocated Dell Laptop (serial: CN-0192) has an expiring warranty next week.");
                    notif1.setReferenceType("WARRANTY");
                    notif1.setReferenceId(3L);
                    notif1.setRead(false);
                    notificationRepository.save(notif1);

                    com.assetflow.notification.Notification notif2 = new com.assetflow.notification.Notification();
                    notif2.setRecipientUserId(emp.getId());
                    notif2.setType(com.assetflow.notification.NotificationType.ASSET_ASSIGNED);
                    notif2.setTitle("Welcome to AssetFlow");
                    notif2.setMessage("Your employee profile has been fully activated. View your assigned equipment in the profile page.");
                    notif2.setReferenceType("SYSTEM");
                    notif2.setReferenceId(4L);
                    notif2.setRead(false);
                    notificationRepository.save(notif2);
                    log.info("Bootstrapped notifications for EMPLOYEE.");
                }
            });
        } catch (Exception e) {
            log.warn("Could not bootstrap notifications (table may not be ready yet): {}", e.getMessage());
        }
    }
}
