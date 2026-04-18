package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDTO {

    private String doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private Boolean isActive;
    private Boolean isValid;

    //    private List<Speciality> speciality;
    private SpecialityRequestDTO speciality;


    //    private WorkZone workZone;
    private String city;
    private String province;
//    private License license;

    private Long license_id;
    private String licenseName;

    private String status;

    private LocalDateTime performedDate;
    private LocalDateTime expiryDate;


}

