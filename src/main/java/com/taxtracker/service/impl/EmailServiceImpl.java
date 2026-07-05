package com.taxtracker.service.impl;

import com.taxtracker.exception.EmailDeliveryException;
import com.taxtracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails (OTP + confirmations) over generic SMTP.
 *
 * SMTP host/port/credentials are read from application.properties
 * (spring.mail.*). When no real SMTP server is configured (the default
 * placeholder host), the message is logged to the console instead of being
 * sent, so the full registration / submission flow works end-to-end during
 * local development without real mail credentials.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:no-reply@taxtracker.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    public void sendOtpEmail(String toEmail, String name, String otp) {
        String subject = "Your TaxTracker verification code";
        String body = "Hello " + safeName(name) + ",\n\n"
                + "Your One-Time Password (OTP) to complete your TaxTracker registration is: " + otp + "\n\n"
                + "This code is valid for 10 minutes. If you did not request this, please ignore this email.\n\n"
                + "- Team TaxTracker";
        boolean delivered = dispatch(toEmail, subject, body);
        if (!delivered) {
            // OTP is useless if it never reached the user — let the caller surface this.
            throw new EmailDeliveryException("app.message.otp.send.failed");
        }
    }

    @Override
    public void sendRegistrationConfirmation(String toEmail, String name) {
        String subject = "Welcome to TaxTracker";
        String body = "Hello " + safeName(name) + ",\n\n"
                + "Your TaxTracker account has been created successfully. "
                + "You can now log in and start managing your tax filings and transactions.\n\n"
                + "- Team TaxTracker";
        dispatch(toEmail, subject, body);
    }

    @Override
    public void sendForm90CConfirmation(String toEmail, String name, String financialYear) {
        String subject = "Form 90C submitted successfully";
        String fy = (financialYear == null || financialYear.isBlank()) ? "the selected financial year" : financialYear;
        String body = "Hello " + safeName(name) + ",\n\n"
                + "Your Form 90C for " + fy + " has been submitted successfully. "
                + "You can view its status anytime from your TaxTracker dashboard.\n\n"
                + "- Team TaxTracker";
        dispatch(toEmail, subject, body);
    }

    private boolean dispatch(String toEmail, String subject, String body) {
        // Fall back to console logging when SMTP isn't configured.
        if (!mailEnabled || mailHost == null || mailHost.isBlank()) {
            log.info("[EMAIL-FALLBACK] (SMTP not configured) ---------------------------");
            log.info("[EMAIL-FALLBACK] To     : {}", toEmail);
            log.info("[EMAIL-FALLBACK] Subject: {}", subject);
            log.info("[EMAIL-FALLBACK] Body   :\n{}", body);
            log.info("[EMAIL-FALLBACK] -------------------------------------------------");
            return true;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", toEmail, subject);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", toEmail, ex.getMessage());
            return false;
        }
    }

    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "there" : name;
    }
}
