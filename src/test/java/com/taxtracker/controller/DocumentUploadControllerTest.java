package com.taxtracker.controller;

import com.taxtracker.dto.UploadResponse;
import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.DocumentUploadService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentUploadController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class DocumentUploadControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DocumentUploadService documentUploadService;
    @MockBean private AuthContext authContext;
    @MockBean private MessageResolver messageResolver;
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;

    @Test
    void upload_returns200() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        when(documentUploadService.upload(eq("prajwal@taxtracker.com"), any()))
                .thenReturn(new UploadResponse(1L, "proof.pdf", "File uploaded successfully."));

        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/uploads").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.fileName").value("proof.pdf"));
    }

    @Test
    void upload_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/uploads").file(file))
                .andExpect(status().isUnauthorized());
    }
}
