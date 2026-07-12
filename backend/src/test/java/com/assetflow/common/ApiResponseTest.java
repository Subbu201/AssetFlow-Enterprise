package com.assetflow.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Success message", "Data");
        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getMessage());
        assertEquals("Data", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Error message");
        assertFalse(response.isSuccess());
        assertEquals("Error message", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }
}
