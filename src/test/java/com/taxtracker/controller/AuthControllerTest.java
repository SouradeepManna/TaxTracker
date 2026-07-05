package com.taxtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxtracker.dto.AddressDTO;
import com.taxtracker.dto.LoginRequest;
import com.taxtracker.dto.LoginResponse;
import com.taxtracker.dto.RegisterRequest;
import com.taxtracker.dto.VerifyOtpRequest;
import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.service.AuthService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private MessageResolver messageResolver;
    // Beans referenced by the component scan / filter wiring
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;
    @MockBean private com.taxtracker.security.AuthContext authContext;

    private RegisterRequest validRegister() {
        AddressDTO a = new AddressDTO();
        a.setAddressLine1("Flat 1");
        a.setArea("Area");
        a.setCity("Bengaluru");
        a.setState("Karnataka");
        a.setPin("560001");

        RegisterRequest r = new RegisterRequest();
        r.setName("Prajwal Koppa");
        r.setEmail("prajwal@taxtracker.com");
        r.setPassword("Password@1");
        r.setMobNumber("9972649525");
        r.setAddress(a);
        return r;
    }

    @Test
    void register_returns200WithMessage() throws Exception {
        when(authService.initiateRegistration(any(RegisterRequest.class)))
                .thenReturn("An OTP has been sent to your email. Please verify to complete registration.");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegister())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("OTP")));
    }

    @Test
    void register_returns400OnInvalidEmail() throws Exception {
        RegisterRequest bad = validRegister();
        bad.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOtp_returns201() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("123456");
        when(authService.verifyOtpAndRegister(any(VerifyOtpRequest.class))).thenReturn("Registration successful");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void verifyOtp_returns400OnBadOtpFormat() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("12"); // too short
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmailId("prajwal@taxtracker.com");
        req.setPassword("Password@1");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt", "Prajwal Koppa", "prajwal@taxtracker.com", "Login successful"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.name").value("Prajwal Koppa"));
    }

    @Test
    void login_returns400OnInvalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmailId("prajwal@taxtracker.com");
        req.setPassword("Password@1");
        when(messageResolver.resolve(any())).thenReturn("The email address or password is incorrect.");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidInputException("app.message.invalid.credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("The email address or password is incorrect."));
    }

    @Test
    void logout_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
