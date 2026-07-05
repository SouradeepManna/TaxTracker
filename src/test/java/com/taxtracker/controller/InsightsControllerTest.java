package com.taxtracker.controller;

import com.taxtracker.dto.InsightsResponse;
import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.InsightsService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InsightsController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class InsightsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private InsightsService insightsService;
    @MockBean private AuthContext authContext;
    @MockBean private MessageResolver messageResolver;
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;

    @Test
    void getInsights_returns200WhenAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        InsightsResponse resp = new InsightsResponse();
        resp.setTotalTransactions(3);
        resp.setTotalTax(BigDecimal.valueOf(450));
        resp.setProjectedNextMonthTax(BigDecimal.valueOf(6517));
        resp.setTaxByType(new ArrayList<>());
        resp.setTopOrganizations(new ArrayList<>());
        resp.setMonthlyTrend(new ArrayList<>());
        resp.setTrendNarrative("steady");
        when(insightsService.getInsights(any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/insights").param("financialYear", "2024-2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(3))
                .andExpect(jsonPath("$.projectedNextMonthTax").value(6517));
    }

    @Test
    void getInsights_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        mockMvc.perform(get("/api/insights"))
                .andExpect(status().isUnauthorized());
    }
}
