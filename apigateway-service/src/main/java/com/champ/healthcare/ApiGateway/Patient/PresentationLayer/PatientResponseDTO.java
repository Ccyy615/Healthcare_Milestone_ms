package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientResponseDTO extends RepresentationModel<PatientResponseDTO> {

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

    @JsonProperty("patientId")
    private void unpackPatientId(Object patientIdValue) {
        if (patientIdValue instanceof Map<?, ?> patientIdMap) {
            Object value = patientIdMap.get("patientId");
            this.patientId = value != null ? value.toString() : null;
            return;
        }

        this.patientId = patientIdValue != null ? patientIdValue.toString() : null;
    }

    @JsonProperty("contactInfo")
    private void unpackContactInfo(Map<String, Object> contactInfo) {
        if (contactInfo == null) {
            return;
        }

        this.email = contactInfo.get("email") != null ? contactInfo.get("email").toString() : null;
        this.phone = contactInfo.get("phone") != null ? contactInfo.get("phone").toString() : null;
    }

    @JsonProperty("address")
    private void unpackAddress(Map<String, Object> address) {
        if (address == null) {
            return;
        }

        this.street = address.get("street") != null ? address.get("street").toString() : null;
        this.city = address.get("city") != null ? address.get("city").toString() : null;
        this.province = address.get("province") != null ? address.get("province").toString() : null;
        this.postal_code = address.get("postal_code") != null ? address.get("postal_code").toString() : null;
        this.country = address.get("country") != null ? address.get("country").toString() : null;
    }

    @JsonProperty("allergy")
    private void unpackAllergy(Map<String, Object> allergy) {
        if (allergy == null) {
            return;
        }

        this.substance = allergy.get("substance") != null ? allergy.get("substance").toString() : null;
        this.reaction = allergy.get("reaction") != null ? allergy.get("reaction").toString() : null;
    }

    @JsonProperty("bloodType")
    private void unpackBloodType(Object bloodTypeValue) {
        this.bloodType = bloodTypeValue != null ? bloodTypeValue.toString() : null;
    }

    @JsonProperty("status")
    private void unpackStatus(Object statusValue) {
        if (statusValue instanceof Map<?, ?> statusMap) {
            Object value = statusMap.get("status");
            this.status = value != null ? new PatientStatusPatchDTO(value.toString()) : null;
            return;
        }

        this.status = statusValue != null ? new PatientStatusPatchDTO(statusValue.toString()) : null;
    }
}
