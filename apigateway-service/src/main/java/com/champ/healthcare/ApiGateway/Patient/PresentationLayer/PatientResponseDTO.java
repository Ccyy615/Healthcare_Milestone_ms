package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {

    private Long id;
    private String patientId;

    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;

    //    private  ContactInfo contactInfo;
    private String email;
    private String phone;

    //    private Address address;
    private String street;
    private String city;
    private String province;
    private String postal_code;
    private String country;

    private String insuranceNumber;

    //    private Allergy allergy;
    private String substance;
    private String reaction;

    private String bloodType;
    private PatientStatusPatchDTO status;
}