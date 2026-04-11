package com.champ.healthcare.Doctor.Mapper;

import com.champ.healthcare.Doctor.Domain.Doctor;
import com.champ.healthcare.Doctor.Domain.*;
import com.champ.healthcare.Doctor.PresentationLayer.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DoctorMapper {

    public Doctor toEntity(DoctorRequestDTO doctorRequestDTO) {
        WorkZone workZone = new WorkZone(
                doctorRequestDTO.getWorkZone().getCity(),
                doctorRequestDTO.getWorkZone().getProvince()
        );

        var speciality = doctorRequestDTO.getSpeciality() != null
                ? doctorRequestDTO.getSpeciality().stream()
                .map(s -> new Speciality(s.getSpeciality(), s.getProficiencyLevel()))
                .collect(Collectors.toList())
                : null;

        return Doctor.builder()
                .doctorId(new DoctorIdentifier())
                .doctorFirstName(doctorRequestDTO.getDoctorFirstName())
                .doctorLastName(doctorRequestDTO.getDoctorLastName())
                .workZone(workZone)
                .speciality(speciality != null ? speciality : new java.util.ArrayList<>())
                .isActive(false)
                .isValid(false)
                .license(null)
                .build();

    }

    public DoctorResponseDTO toResponseDTO (Doctor entity) {
        DoctorResponseDTO doctorResponseDTO = new DoctorResponseDTO();
        doctorResponseDTO.setDoctorId(entity.getDoctorId().getDoctorId());
        doctorResponseDTO.setDoctorFirstName(entity.getDoctorFirstName());
        doctorResponseDTO.setDoctorLastName(entity.getDoctorLastName());
        doctorResponseDTO.setIsActive(entity.getIsActive());
        doctorResponseDTO.setIsValid(entity.getIsValid());

        if (entity.getWorkZone() != null) {
            WorkZone workZoneDTO = new WorkZone();
            workZoneDTO.setCity(entity.getWorkZone().getCity());
            workZoneDTO.setProvince(entity.getWorkZone().getProvince());
            doctorResponseDTO.setWorkZone(workZoneDTO);
        }

        if (entity.getSpeciality() != null) {
            var specialityDTO = entity.getSpeciality().stream()
                    .map(s -> {
                        Speciality speciality = new Speciality();
                        speciality.setSpeciality(s.getSpeciality());
                        speciality.setProficiencyLevel(s.getProficiencyLevel());
                        return speciality;
                    })
                    .collect(Collectors.toList());
            doctorResponseDTO.setSpeciality(specialityDTO);
        }

        if (entity.getLicense() != null) {
            License licenseDTO = new License();

            licenseDTO.setLicense_id(entity.getLicense().getLicense_id());
            licenseDTO.setLicenseName(entity.getLicense().getLicenseName());
            licenseDTO.setStatus(entity.getLicense().getStatus());
            licenseDTO.setPerformedDate(entity.getLicense().getPerformedDate());
            licenseDTO.setExpiryDate(entity.getLicense().getExpiryDate());

            doctorResponseDTO.setLicense(licenseDTO);
        }

        return doctorResponseDTO;
    }
}
