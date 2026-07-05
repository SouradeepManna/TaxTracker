package com.taxtracker.service;

import com.taxtracker.dto.AddressDTO;
import com.taxtracker.dto.LoginRequest;
import com.taxtracker.dto.LoginResponse;
import com.taxtracker.dto.RegisterRequest;
import com.taxtracker.dto.VerifyOtpRequest;
import com.taxtracker.entity.PendingRegistrationEntity;
import com.taxtracker.entity.UserEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.PendingRegistrationRepository;
import com.taxtracker.repository.UserRepository;
import com.taxtracker.security.JwtUtil;
import com.taxtracker.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PendingRegistrationRepository pendingRegistrationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailService emailService;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        AddressDTO address = new AddressDTO();
        address.setAddressLine1("Flat 1");
        address.setAddressLine2("Street");
        address.setArea("Area");
        address.setCity("Bengaluru");
        address.setState("Karnataka");
        address.setPin("560001");

        registerRequest = new RegisterRequest();
        registerRequest.setName("Prajwal Koppa");
        registerRequest.setEmail("prajwal@taxtracker.com");
        registerRequest.setPassword("Password@1");
        registerRequest.setMobNumber("9972649525");
        registerRequest.setAddress(address);
    }

    @Test
    void initiateRegistration_savesPendingAndSendsOtp() throws TaxTrackerException {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(pendingRegistrationRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");

        String result = authService.initiateRegistration(registerRequest);

        assertTrue(result.toLowerCase().contains("otp"));
        ArgumentCaptor<PendingRegistrationEntity> captor = ArgumentCaptor.forClass(PendingRegistrationEntity.class);
        verify(pendingRegistrationRepository).save(captor.capture());
        PendingRegistrationEntity saved = captor.getValue();
        assertEquals("prajwal@taxtracker.com", saved.getEmail());
        assertEquals("hashed", saved.getPassword());
        assertEquals(6, saved.getOtp().length());
        assertTrue(saved.getOtpExpiry().isAfter(LocalDateTime.now()));
        verify(emailService).sendOtpEmail(eq("prajwal@taxtracker.com"), eq("Prajwal Koppa"), anyString());
    }

    @Test
    void initiateRegistration_replacesExistingPending() throws TaxTrackerException {
        PendingRegistrationEntity existing = new PendingRegistrationEntity();
        existing.setEmail(registerRequest.getEmail());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(pendingRegistrationRepository.findByEmail(anyString())).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        authService.initiateRegistration(registerRequest);

        verify(pendingRegistrationRepository).delete(existing);
        verify(pendingRegistrationRepository).save(any(PendingRegistrationEntity.class));
    }

    @Test
    void initiateRegistration_propagatesEmailDeliveryFailure() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(pendingRegistrationRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");
        doThrow(new com.taxtracker.exception.EmailDeliveryException("app.message.otp.send.failed"))
                .when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        assertThrows(com.taxtracker.exception.EmailDeliveryException.class,
                () -> authService.initiateRegistration(registerRequest));
    }

    @Test
    void initiateRegistration_throwsWhenUserExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authService.initiateRegistration(registerRequest));
        assertEquals("app.message.user.already.exists", ex.getMessage());
        verify(pendingRegistrationRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void verifyOtpAndRegister_createsUserAndSendsConfirmation() throws TaxTrackerException {
        PendingRegistrationEntity pending = buildPending("123456", LocalDateTime.now().plusMinutes(5));
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("123456");

        when(pendingRegistrationRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(Optional.of(pending));
        when(userRepository.existsByEmail("prajwal@taxtracker.com")).thenReturn(false);

        String result = authService.verifyOtpAndRegister(req);

        assertEquals("Registration successful", result);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("prajwal@taxtracker.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPassword());
        verify(pendingRegistrationRepository).delete(pending);
        verify(emailService).sendRegistrationConfirmation("prajwal@taxtracker.com", "Prajwal Koppa");
    }

    @Test
    void verifyOtpAndRegister_throwsWhenNoPending() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("missing@taxtracker.com");
        req.setOtp("123456");
        when(pendingRegistrationRepository.findByEmail("missing@taxtracker.com")).thenReturn(Optional.empty());

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authService.verifyOtpAndRegister(req));
        assertEquals("app.message.otp.not.found", ex.getMessage());
    }

    @Test
    void verifyOtpAndRegister_throwsAndDeletesWhenExpired() {
        PendingRegistrationEntity pending = buildPending("123456", LocalDateTime.now().minusMinutes(1));
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("123456");
        when(pendingRegistrationRepository.findByEmail(anyString())).thenReturn(Optional.of(pending));

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authService.verifyOtpAndRegister(req));
        assertEquals("app.message.otp.expired", ex.getMessage());
        verify(pendingRegistrationRepository).delete(pending);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtpAndRegister_throwsWhenOtpMismatch() {
        PendingRegistrationEntity pending = buildPending("123456", LocalDateTime.now().plusMinutes(5));
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("999999");
        when(pendingRegistrationRepository.findByEmail(anyString())).thenReturn(Optional.of(pending));

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authService.verifyOtpAndRegister(req));
        assertEquals("app.message.otp.invalid", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtpAndRegister_throwsWhenUserRaceExists() {
        PendingRegistrationEntity pending = buildPending("123456", LocalDateTime.now().plusMinutes(5));
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("prajwal@taxtracker.com");
        req.setOtp("123456");
        when(pendingRegistrationRepository.findByEmail(anyString())).thenReturn(Optional.of(pending));
        when(userRepository.existsByEmail("prajwal@taxtracker.com")).thenReturn(true);

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authService.verifyOtpAndRegister(req));
        assertEquals("app.message.user.already.exists", ex.getMessage());
        verify(pendingRegistrationRepository).delete(pending);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_succeedsWithValidCredentials() throws TaxTrackerException {
        UserEntity user = new UserEntity();
        user.setEmail("prajwal@taxtracker.com");
        user.setName("Prajwal Koppa");
        user.setPassword("hashed");

        LoginRequest req = new LoginRequest();
        req.setEmailId("prajwal@taxtracker.com");
        req.setPassword("Password@1");

        when(userRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("prajwal@taxtracker.com")).thenReturn("jwt-token");

        LoginResponse res = authService.login(req);

        assertEquals("jwt-token", res.getToken());
        assertEquals("Prajwal Koppa", res.getName());
        assertEquals("Login successful", res.getMessage());
    }

    @Test
    void login_throwsWhenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmailId("missing@taxtracker.com");
        req.setPassword("Password@1");
        when(userRepository.findByEmail("missing@taxtracker.com")).thenReturn(Optional.empty());

        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> authService.login(req));
        assertEquals("app.message.invalid.credentials", ex.getMessage());
    }

    @Test
    void login_throwsWhenPasswordWrong() {
        UserEntity user = new UserEntity();
        user.setEmail("prajwal@taxtracker.com");
        user.setPassword("hashed");

        LoginRequest req = new LoginRequest();
        req.setEmailId("prajwal@taxtracker.com");
        req.setPassword("WrongPass@1");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass@1", "hashed")).thenReturn(false);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> authService.login(req));
        assertEquals("app.message.invalid.credentials", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    private PendingRegistrationEntity buildPending(String otp, LocalDateTime expiry) {
        PendingRegistrationEntity pending = new PendingRegistrationEntity();
        pending.setName("Prajwal Koppa");
        pending.setEmail("prajwal@taxtracker.com");
        pending.setPassword("hashed");
        pending.setMobNumber("9972649525");
        pending.setOtp(otp);
        pending.setOtpExpiry(expiry);
        return pending;
    }
}
