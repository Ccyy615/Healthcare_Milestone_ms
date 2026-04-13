package com.champ.healthcare.Doctor.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_license")
@Getter
@Setter
@NoArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long license_id;

    @Column(name = "license_name", nullable = false)
    private String licenseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private LicenseStatus status;

    @Column(name = "performed_date", nullable = false)
    private LocalDateTime performedDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    public License(String licenseName,LicenseStatus status, LocalDateTime performedDate) {
        this.licenseName = licenseName;
        this.status = status;
        this.performedDate = performedDate;

        if (status == LicenseStatus.VALID) {
            this.expiryDate = performedDate.plusYears(5);
        }
    }

    public void updateStatus(String licenseName,LicenseStatus newStatus) {
        this.licenseName = licenseName;
        this.status = newStatus;
        if (newStatus == LicenseStatus.VALID) {
            this.expiryDate = LocalDateTime.now().plusYears(5);
        }
    }

    public boolean isValid() {
        return status == LicenseStatus.VALID
                && (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

}
