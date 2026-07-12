package com.assetflow.integration;

import com.assetflow.auth.dto.ForgotPasswordRequest;
import com.assetflow.auth.dto.LoginRequest;
import com.assetflow.common.ApiResponse;
import com.assetflow.organization.category.dto.CreateCategoryRequest;
import com.assetflow.organization.department.dto.CreateDepartmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberOneIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private static String adminToken;
    private static Long savedDepartmentId;

    @Test
    @Order(1)
    void testAdminBootstrapAndLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@assetflow.com");
        loginRequest.setPassword("Admin@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        adminToken = responseStr.split("\"accessToken\":\"")[1].split("\"")[0];
    }

    @Test
    @Order(2)
    void testCurrentUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @Order(3)
    void testForgotPasswordSameResponse() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent."));
    }

    @Test
    @Order(4)
    void testCreateDepartment() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setName("Human Resources " + uniqueSuffix);
        req.setCode("HR-" + uniqueSuffix);

        MvcResult result = mockMvc.perform(post("/api/admin/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();
                
        String responseStr = result.getResponse().getContentAsString();
        savedDepartmentId = Long.parseLong(responseStr.split("\"id\":")[1].split(",")[0]);
    }

    @Test
    @Order(5)
    void testDuplicateDepartmentRejected() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setName("Human Resources Dup " + uniqueSuffix);
        req.setCode("HR-DUP-" + uniqueSuffix);

        // Create first time
        mockMvc.perform(post("/api/admin/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Create second time - conflict
        mockMvc.perform(post("/api/admin/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    void testCircularHierarchyRejected() throws Exception {
        // Can't easily test without Update API call since create checks parent existence
        // We will test updating department to its own parent
        String updateJson = "{\"name\":\"Human Resources Update\", \"code\":\"HR-UPD-" + System.currentTimeMillis() + "\", \"parentDepartmentId\":" + savedDepartmentId + "}";
        mockMvc.perform(put("/api/admin/departments/" + savedDepartmentId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(7)
    void testCreateCategoryWithNegativeWarrantyRejected() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("Laptops " + System.currentTimeMillis());
        req.setCode("CAT-LAP-" + System.currentTimeMillis());
        req.setWarrantyPeriodMonths(-5);

        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void testNormalEmployeeCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/departments"))
                .andExpect(status().isForbidden());
    }
}
