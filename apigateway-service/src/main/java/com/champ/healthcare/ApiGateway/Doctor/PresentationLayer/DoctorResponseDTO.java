package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorResponseDTO extends RepresentationModel<DoctorResponseDTO> {

    private String doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private Boolean isActive;
    private Boolean isValid;

    private SpecialityRequestDTO speciality;
    private String city;
    private String province;
    private Long license_id;
    private String licenseName;
    private String status;
    private LocalDateTime performedDate;
    private LocalDateTime expiryDate;

    @JsonProperty("speciality")
    public void unpackSpeciality(JsonNode specialityNode) {
        if (specialityNode == null || specialityNode.isNull()) {
            this.speciality = null;
            return;
        }

        JsonNode firstSpeciality = specialityNode.isArray() ? specialityNode.path(0) : specialityNode;
        if (firstSpeciality.isMissingNode() || firstSpeciality.isNull()) {
            this.speciality = null;
            return;
        }

        this.speciality = new SpecialityRequestDTO(
                firstSpeciality.path("speciality").asText(null),
                firstSpeciality.path("proficiencyLevel").asText(null)
        );
    }

    @JsonProperty("workZone")
    public void unpackWorkZone(JsonNode workZoneNode) {
        if (workZoneNode == null || workZoneNode.isNull()) {
            this.city = null;
            this.province = null;
            return;
        }

        this.city = workZoneNode.path("city").asText(null);
        this.province = workZoneNode.path("province").asText(null);
    }

    @JsonProperty("license")
    public void unpackLicense(JsonNode licenseNode) {
        if (licenseNode == null || licenseNode.isNull()) {
            this.license_id = null;
            this.licenseName = null;
            this.status = null;
            this.performedDate = null;
            this.expiryDate = null;
            return;
        }

        this.license_id = licenseNode.hasNonNull("license_id") ? licenseNode.get("license_id").asLong() : null;
        this.licenseName = licenseNode.path("licenseName").asText(null);
        this.status = licenseNode.path("status").asText(null);
        this.performedDate = parseDateTime(licenseNode.get("performedDate"));
        this.expiryDate = parseDateTime(licenseNode.get("expiryDate"));
    }

    private LocalDateTime parseDateTime(JsonNode node) {
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return LocalDateTime.parse(node.asText());
    }

}

