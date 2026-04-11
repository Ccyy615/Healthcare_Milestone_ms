package com.champ.healthcare.Patient.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Allergy {

    @Column(name = "allergy_substance")
    private String substance;

    @Column(name = "allergy_reaction")
    private String reaction;
}