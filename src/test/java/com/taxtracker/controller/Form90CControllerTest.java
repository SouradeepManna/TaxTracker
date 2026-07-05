package com.taxtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxtracker.dto.Form90CRequest;
import com.taxtracker.dto.Form90CResponse;
import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.Form90CService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = Form90CController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class Form90CControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private Form90CService form90CService;
    @MockBean private AuthContext authContext;
    @MockBean private MessageResolver messageResolver;
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;

    private Form90CRequest validRequest() {
        Form90CRequest req = new Form90CRequest();
        req.setName("Prajwal Koppa");
        req.setMobileNumber("9972649525");
        req.setFinancialYear("2024-2025");
        req.setTransactionHistory(new ArrayList<>());
        return req;
    }

    @Test
    void saveForm_returns200() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        Form90CResponse resp = new Form90CResponse();
        resp.setFormId(1L);
        resp.setStatus("DRAFT");
        when(form90CService.saveForm(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/forms/90c").param("status", "DRAFT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void saveForm_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        mockMvc.perform(post("/api/forms/90c")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyForm_returns200() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        Form90CResponse resp = new Form90CResponse();
        resp.setFormId(7L);
        when(form90CService.getForm("prajwal@taxtracker.com")).thenReturn(resp);

        mockMvc.perform(get("/api/forms/90c/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(7));
    }
}
