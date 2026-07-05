package com.taxtracker.controller;

import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.DataExportService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DataExportController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class DataExportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DataExportService dataExportService;
    @MockBean private AuthContext authContext;
    @MockBean private MessageResolver messageResolver;
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;

    @Test
    void download_returns200WithFile() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        when(dataExportService.export(any(), any(), any())).thenReturn("[]".getBytes());
        when(dataExportService.fileName(any())).thenReturn("transactions.json");
        when(dataExportService.contentType(any())).thenReturn("application/json");

        mockMvc.perform(get("/api/transactions/download").param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("transactions.json")));
    }

    @Test
    void download_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        mockMvc.perform(get("/api/transactions/download").param("format", "json"))
                .andExpect(status().isUnauthorized());
    }
}
