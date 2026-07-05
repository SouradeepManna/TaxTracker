package com.taxtracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Holds a registration that has been started but not yet OTP-verified.
 * Once the user submits the correct OTP, a real UserEntity is created and
 * the matching row here is removed.
 */
@Data
@Entity
@Table(name = "pending_registration")
public class PendingRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_id")
    private Long pendingId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // already BCrypt-hashed before being stored here
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "mob_number", length = 10)
    private String mobNumber;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pin", length = 6)
    private String pin;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "otp_expiry", nullable = false)
    private LocalDateTime otpExpiry;
}
