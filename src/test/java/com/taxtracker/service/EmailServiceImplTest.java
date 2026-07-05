package com.taxtracker.service;

import com.taxtracker.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailServiceImpl emailService;

    private void enableSmtp() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "mailHost", "smtp.example.com");
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@taxtracker.com");
    }

    private void disableSmtp() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);
        ReflectionTestUtils.setField(emailService, "mailHost", "");
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@taxtracker.com");
    }

    @Test
    void sendOtp_usesConsoleFallbackWhenDisabled() {
        disableSmtp();
        emailService.sendOtpEmail("prajwal@taxtracker.com", "Prajwal", "123456");
        verifyNoInteractions(mailSender);
    }

    @Test
    void sendOtp_sendsMailWhenEnabled() {
        enableSmtp();
        emailService.sendOtpEmail("prajwal@taxtracker.com", "Prajwal", "123456");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendRegistrationConfirmation_sendsMailWhenEnabled() {
        enableSmtp();
        emailService.sendRegistrationConfirmation("prajwal@taxtracker.com", "Prajwal");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendForm90CConfirmation_sendsMailWhenEnabled() {
        enableSmtp();
        emailService.sendForm90CConfirmation("prajwal@taxtracker.com", "Prajwal", "2024-2025");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendForm90CConfirmation_handlesBlankNameAndFy() {
        enableSmtp();
        emailService.sendForm90CConfirmation("prajwal@taxtracker.com", "", "");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtp_throwsEmailDeliveryExceptionWhenSendFails() {
        enableSmtp();
        doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        // OTP delivery failure must surface so the user can retry.
        assertThrows(com.taxtracker.exception.EmailDeliveryException.class,
                () -> emailService.sendOtpEmail("prajwal@taxtracker.com", "Prajwal", "123456"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendConfirmation_doesNotThrowWhenSendFails() {
        enableSmtp();
        doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        // Confirmation emails are non-blocking — account already exists.
        emailService.sendRegistrationConfirmation("prajwal@taxtracker.com", "Prajwal");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
