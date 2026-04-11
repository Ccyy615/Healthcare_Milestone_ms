package com.champ.healthcare.Doctor.Domain;

import com.champ.healthcare.utilities.DoctorNotEligibleException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Embedded
    private DoctorIdentifier doctorId;

    @Column(name = "first_name")
    private String doctorFirstName;

    @Column(name = "last_name")
    private String doctorLastName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "work_zone_city")),
            @AttributeOverride(name = "province", column = @Column(name = "work_zone_province"))
    })
    private WorkZone workZone;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "doctor_specialities",
            joinColumns = @JoinColumn(
                    name = "doctor_id",
                    referencedColumnName = "id"
            )
    )
    private List<Speciality> speciality = new ArrayList<>();

    @OneToOne(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private License license;

//    /**
//     * Invariant
//     *
//     */

    public void activate() {

        if (!hasVerifiedSpeciality()) {
            throw new DoctorNotEligibleException(
                    "Cannot activate Doctor: No verified speciality found."
            );
        }

        if (!hasValidLicense()) {
            throw new DoctorNotEligibleException(
                    "Cannot activate handyman: No valid license found. "
            );
        }
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void verify() {
        this.isValid = true;
    }

    public void unverify() {
        this.isValid = false;
        this.isActive = false;
    }

    public void addSpeciality(Speciality specialitySet) {
        specialitySet.validate();
        this.speciality.add(specialitySet);
    }

    public void removeSpeciality(String specialityName) {
        this.speciality.removeIf(speciality -> speciality.getSpeciality().equalsIgnoreCase(specialityName));

        if (this.isActive && !hasVerifiedSpeciality()) {
            this.isActive = false;
        }
    }

    public void updateWorkZone(WorkZone newWorkZone) {
        newWorkZone.validate();
        this.workZone = newWorkZone;
    }

    public void setLicense(License license) {
        this.license = license;

        if (license != null) {
            license.setDoctor(this);
        }
    }



    private boolean hasVerifiedSpeciality() {
        return speciality.stream().anyMatch(Speciality::isVerified);
    }

    public boolean hasValidLicense() {
        return license != null && license.isValid();
    }







}
