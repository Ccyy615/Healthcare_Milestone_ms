package com.champ.healthcare.Doctor.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {

    @Column(name = "speciality_name")
    private String speciality;

    @Column(name = "proficiency_level")
    private ProficiencyLevel proficiencyLevel;

    public void validate() {
        if (speciality == null || speciality.trim().isEmpty()) {
            throw new IllegalArgumentException("speciality type is required.");
        }
        if (proficiencyLevel == null) {
            throw new IllegalArgumentException("Proficiency level is required.");
        }
    }

    public boolean isVerified() {
        return proficiencyLevel == ProficiencyLevel.INTERMEDIATE
                || proficiencyLevel == ProficiencyLevel.ADVANCED
                || proficiencyLevel == ProficiencyLevel.EXPERT;
    }
}