package com.champ.healthcare.Patient.Domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PatientIdentifier patientId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "email", column = @Column(name = "contact_email")),
            @AttributeOverride(name = "phone", column = @Column(name = "contact_phone"))
    })
    private ContactInfo contactInfo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "street")),
            @AttributeOverride(name = "city", column = @Column(name = "city")),
            @AttributeOverride(name = "province", column = @Column(name = "province")),
            @AttributeOverride(name = "postal_code", column = @Column(name = "postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "country"))
    })
    private Address address;

    @Column(name = "insurancenumber", nullable = false)
    private String insuranceNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "substance", column = @Column(name = "allergy_substance")),
            @AttributeOverride(name = "reaction", column = @Column(name = "allergy_reaction"))
    })
    private Allergy allergy;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_status", nullable = false)
    private PatientStatus status;

    public Patient(
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            ContactInfo contactInfo,
            String insuranceNumber,
            Allergy allergy,
            BloodType bloodType,
            PatientStatus status
    ) {
        validateContactInfo(contactInfo);

        this.patientId = new PatientIdentifier();
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contactInfo = contactInfo;
        this.insuranceNumber = insuranceNumber;
        this.allergy = allergy;
        this.bloodType = bloodType;
        this.status = status;
    }

    public void updateContactInfo(ContactInfo newContactInfo) {
        validateContactInfo(newContactInfo);
        this.contactInfo = newContactInfo;
    }

    public void deactivate() {
        this.status = PatientStatus.INACTIVE;
    }

    public void activate() {
        this.status = PatientStatus.ACTIVE;
    }

    public void updateEmail(String newEmail) {
        validateEmail(newEmail);
        if (this.contactInfo == null) {
            this.contactInfo = new ContactInfo();
        }
        this.contactInfo.setEmail(newEmail);
    }

    private void validateContactInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            throw new IllegalArgumentException(
                    "Patient must have at least one valid contact method: email or phone."
            );
        }

        boolean hasEmail = StringUtils.hasText(contactInfo.getEmail());
        boolean hasPhone = StringUtils.hasText(contactInfo.getPhone());

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException(
                    "Patient must have at least one valid contact method: email or phone."
            );
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
