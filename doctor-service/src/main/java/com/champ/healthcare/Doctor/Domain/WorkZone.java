package com.champ.healthcare.Doctor.Domain;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkZone {
    private String city;
    private String province;

    public void validate() {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (province == null || province.trim().isEmpty()) {
            throw new IllegalArgumentException("Province is required");
        }
    }
}