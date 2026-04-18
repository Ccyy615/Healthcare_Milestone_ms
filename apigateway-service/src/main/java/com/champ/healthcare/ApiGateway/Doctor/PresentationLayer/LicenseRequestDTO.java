package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LicenseRequestDTO {

    private String licenseName;

    @NotNull(message = "Ststus is requires")
    private String status;

    private LocalDateTime performedDate;

}
