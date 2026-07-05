package com.taxtracker.service.impl;

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
import com.taxtracker.service.AuthService;
import com.taxtracker.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int OTP_VALIDITY_MINUTES = 10;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public String initiateRegistration(RegisterRequest request) throws TaxTrackerException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidInputException("app.message.user.already.exists");
        }

        // If a pending registration already exists for this email, replace it
        // so a fresh OTP is issued.
        pendingRegistrationRepository.findByEmail(request.getEmail())
                .ifPresent(pendingRegistrationRepository::delete);

        String otp = generateOtp();

        PendingRegistrationEntity pending = new PendingRegistrationEntity();
        pending.setName(request.getName());
        pending.setEmail(request.getEmail());
        pending.setPassword(passwordEncoder.encode(request.getPassword()));
        pending.setMobNumber(request.getMobNumber());
        if (request.getAddress() != null) {
            pending.setAddressLine1(request.getAddress().getAddressLine1());
            pending.setAddressLine2(request.getAddress().getAddressLine2());
            pending.setArea(request.getAddress().getArea());
            pending.setCity(request.getAddress().getCity());
            pending.setState(request.getAddress().getState());
            pending.setPin(request.getAddress().getPin());
        }
        pending.setOtp(otp);
        pending.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        pendingRegistrationRepository.save(pending);

        emailService.sendOtpEmail(request.getEmail(), request.getName(), otp);

        return "An OTP has been sent to your email. Please verify to complete registration.";
    }

    @Override
    @Transactional
    public String verifyOtpAndRegister(VerifyOtpRequest request) throws TaxTrackerException {
        PendingRegistrationEntity pending = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidInputException("app.message.otp.not.found"));

        if (pending.getOtpExpiry().isBefore(LocalDateTime.now())) {
            pendingRegistrationRepository.delete(pending);
            throw new InvalidInputException("app.message.otp.expired");
        }

        if (!pending.getOtp().equals(request.getOtp())) {
            throw new InvalidInputException("app.message.otp.invalid");
        }

        // Guard against a race where the user got created in the meantime.
        if (userRepository.existsByEmail(pending.getEmail())) {
            pendingRegistrationRepository.delete(pending);
            throw new InvalidInputException("app.message.user.already.exists");
        }

        UserEntity user = new UserEntity();
        user.setName(pending.getName());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword()); // already hashed
        user.setMobNumber(pending.getMobNumber());
        user.setAddressLine1(pending.getAddressLine1());
        user.setAddressLine2(pending.getAddressLine2());
        user.setArea(pending.getArea());
        user.setCity(pending.getCity());
        user.setState(pending.getState());
        user.setPin(pending.getPin());
        userRepository.save(user);

        pendingRegistrationRepository.delete(pending);

        emailService.sendRegistrationConfirmation(user.getEmail(), user.getName());

        return "Registration successful";
    }

    @Override
    public LoginResponse login(LoginRequest request) throws TaxTrackerException {
        UserEntity user = userRepository.findByEmail(request.getEmailId())
                .orElseThrow(() -> new InvalidInputException("app.message.invalid.credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidInputException("app.message.invalid.credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, user.getName(), user.getEmail(), "Login successful");
    }

    private String generateOtp() {
        int value = 100000 + random.nextInt(900000); // 6-digit
        return String.valueOf(value);
    }
}
