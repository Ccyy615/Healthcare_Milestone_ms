package com.champ.healthcare.Doctor.BusinessLogicLayer;

import com.champ.healthcare.Doctor.DataAccessLayer.DoctorRepository;
import com.champ.healthcare.Doctor.Domain.*;
import com.champ.healthcare.Doctor.Mapper.DoctorMapper;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.Doctor.PresentationLayer.LicenseRequestDTO;
import com.champ.healthcare.Doctor.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Doctor.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceUnitTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void getAllDoctorsReturnsMappedDoctors() {
        Doctor first = doctor("doctor-1");
        Doctor second = doctor("doctor-2");
        DoctorResponseDTO firstResponse = response("doctor-1");
        DoctorResponseDTO secondResponse = response("doctor-2");

        when(doctorRepository.findAll()).thenReturn(List.of(first, second));
        when(doctorMapper.toResponseDTO(first)).thenReturn(firstResponse);
        when(doctorMapper.toResponseDTO(second)).thenReturn(secondResponse);

        List<DoctorResponseDTO> result = doctorService.getAllDoctors();

        assertThat(result).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void getDoctorByIdThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: missing");
    }

    @Test
    void getDoctorByIdReturnsMappedDoctor() {
        Doctor doctor = doctor("doctor-1");
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.getDoctorById("doctor-1");

        assertThat(result).isSameAs(response);
    }

    @Test
    void createDoctorSavesMappedDoctor() {
        DoctorRequestDTO request = request();
        Doctor mapped = doctor("doctor-1");
        DoctorResponseDTO response = response("doctor-1");

        when(doctorMapper.toEntity(request)).thenReturn(mapped);
        when(doctorRepository.save(mapped)).thenReturn(mapped);
        when(doctorMapper.toResponseDTO(mapped)).thenReturn(response);

        DoctorResponseDTO result = doctorService.createDoctor(request);

        assertThat(result).isSameAs(response);
    }

    @Test
    void updateDoctorPreservesExistingIdentifiersAndFlags() {
        Doctor existing = doctor("doctor-1");
        existing.setId(10L);
        existing.setIsActive(true);
        existing.setIsValid(true);
        License existingLicense = new License("Existing", LicenseStatus.VALID, LocalDateTime.now().minusDays(1));
        existing.setLicense(existingLicense);

        Doctor mapped = doctor("new-generated");
        DoctorRequestDTO request = request();
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(existing));
        when(doctorMapper.toEntity(request)).thenReturn(mapped);
        when(doctorRepository.save(mapped)).thenReturn(mapped);
        when(doctorMapper.toResponseDTO(mapped)).thenReturn(response);

        DoctorResponseDTO result = doctorService.updateDoctor("doctor-1", request);

        assertThat(result).isSameAs(response);
        assertThat(mapped.getId()).isEqualTo(10L);
        assertThat(mapped.getDoctorId().getDoctorId()).isEqualTo("doctor-1");
        assertThat(mapped.getIsActive()).isTrue();
        assertThat(mapped.getIsValid()).isTrue();
        assertThat(mapped.getLicense()).isSameAs(existingLicense);
    }

    @Test
    void updateDoctorThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctor("doctor-1", request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void getActiveDoctorsReturnsMappedDoctors() {
        Doctor activeDoctor = doctor("doctor-1");
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByIsActiveTrue()).thenReturn(List.of(activeDoctor));
        when(doctorMapper.toResponseDTO(activeDoctor)).thenReturn(response);

        List<DoctorResponseDTO> result = doctorService.getActiveDoctors();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getActiveDoctorBySpecialityReturnsMappedDoctors() {
        Doctor activeDoctor = doctor("doctor-1");
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByIsActiveTrueAndSpecialityIn(anyList()))
                .thenReturn(List.of(activeDoctor));
        when(doctorMapper.toResponseDTO(activeDoctor)).thenReturn(response);

        List<DoctorResponseDTO> result = doctorService.getActiveDoctorBySpeciality("Cardiology");

        assertThat(result).containsExactly(response);
    }

    @Test
    void activateDoctorRethrowsEligibilityFailure() {
        Doctor doctor = doctor("doctor-1");
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.BEGINNER))));
        doctor.setLicense(new License("Valid License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.activateDoctor("doctor-1"))
                .isInstanceOf(DoctorNotEligibleException.class)
                .hasMessage("Cannot activate Doctor: No verified speciality found.");
    }

    @Test
    void activateDoctorThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.activateDoctor("doctor-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void activateDoctorActivatesAndSavesDoctor() {
        Doctor doctor = doctor("doctor-1");
        doctor.setIsActive(false);
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT))));
        doctor.setLicense(new License("Valid License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.activateDoctor("doctor-1");

        assertThat(result).isSameAs(response);
        assertThat(doctor.getIsActive()).isTrue();
    }

    @Test
    void deactivateDoctorThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deactivateDoctor("doctor-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void deactivateDoctorUpdatesDoctorState() {
        Doctor doctor = doctor("doctor-1");
        doctor.setIsActive(true);
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.deactivateDoctor("doctor-1");

        assertThat(result).isSameAs(response);
        assertThat(doctor.getIsActive()).isFalse();
    }

    @Test
    void updateDoctorActivationDelegatesToDeactivateWhenFalse() {
        Doctor doctor = doctor("doctor-1");
        doctor.setIsActive(true);
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.updateDoctorActivation("doctor-1", false);

        assertThat(result).isSameAs(response);
        assertThat(doctor.getIsActive()).isFalse();
    }

    @Test
    void updateDoctorActivationDelegatesToActivateWhenTrue() {
        Doctor doctor = doctor("doctor-1");
        doctor.setIsActive(false);
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT))));
        doctor.setLicense(new License("Valid License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.updateDoctorActivation("doctor-1", true);

        assertThat(result).isSameAs(response);
        assertThat(doctor.getIsActive()).isTrue();
    }

    @Test
    void updateDoctorActivationThrowsWhenFlagIsMissing() {
        assertThatThrownBy(() -> doctorService.updateDoctorActivation("doctor-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Doctor activation flag is required.");
    }

    @Test
    void addSpecialityThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.addSpeciality(
                "doctor-1",
                new Speciality("Cardiology", ProficiencyLevel.ADVANCED)
        )).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void addSpecialityAddsValidatedSpeciality() {
        Doctor doctor = doctor("doctor-1");
        Speciality speciality = new Speciality("Cardiology", ProficiencyLevel.ADVANCED);
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.addSpeciality("doctor-1", speciality);

        assertThat(result).isSameAs(response);
        assertThat(doctor.getSpeciality()).hasSize(1);
    }

    @Test
    void removeSpecialityThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.removeSpeciality("doctor-1", "Cardiology"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void removeSpecialityRemovesMatchingSpeciality() {
        Doctor doctor = doctor("doctor-1");
        doctor.setSpeciality(new ArrayList<>(List.of(
                new Speciality("Cardiology", ProficiencyLevel.EXPERT),
                new Speciality("Pediatrics", ProficiencyLevel.ADVANCED)
        )));
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.removeSpeciality("doctor-1", "Cardiology");

        assertThat(result).isSameAs(response);
        assertThat(doctor.getSpeciality()).extracting(Speciality::getSpeciality).containsExactly("Pediatrics");
    }

    @Test
    void addLicenseThrowsWhenDoctorIsMissing() {
        LicenseRequestDTO requestDTO = new LicenseRequestDTO();
        requestDTO.setLicenseName("Practice License");
        requestDTO.setStatus(LicenseStatus.VALID);

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.addLicense("doctor-1", requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void addLicenseCreatesAndAssignsLicense() {
        Doctor doctor = doctor("doctor-1");
        LicenseRequestDTO requestDTO = new LicenseRequestDTO();
        requestDTO.setLicenseName("Practice License");
        requestDTO.setStatus(LicenseStatus.VALID);
        DoctorResponseDTO response = response("doctor-1");

        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.addLicense("doctor-1", requestDTO);

        assertThat(result).isSameAs(response);
        assertThat(doctor.getLicense()).isNotNull();
        assertThat(doctor.getLicense().getLicenseName()).isEqualTo("Practice License");
        assertThat(doctor.getLicense().getStatus()).isEqualTo(LicenseStatus.VALID);
    }

    @Test
    void deleteDoctorThrowsWhenDoctorIsMissing() {
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor("doctor-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with ID: doctor-1");
    }

    @Test
    void deleteDoctorDeletesRepositoryEntity() {
        Doctor doctor = doctor("doctor-1");
        when(doctorRepository.findByDoctorId_DoctorId("doctor-1")).thenReturn(Optional.of(doctor));

        doctorService.deleteDoctor("doctor-1");

        verify(doctorRepository).delete(doctor);
    }

    private DoctorRequestDTO request() {
        return new DoctorRequestDTO(
                "John",
                "Smith",
                new WorkZone("Montreal", "Quebec"),
                List.of(new Speciality("Cardiology", ProficiencyLevel.ADVANCED))
        );
    }

    private Doctor doctor(String doctorId) {
        return Doctor.builder()
                .id(1L)
                .doctorId(new DoctorIdentifier(doctorId))
                .doctorFirstName("John")
                .doctorLastName("Smith")
                .isActive(false)
                .isValid(false)
                .workZone(new WorkZone("Montreal", "Quebec"))
                .speciality(new ArrayList<>())
                .build();
    }

    private DoctorResponseDTO response(String doctorId) {
        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId(doctorId);
        response.setDoctorFirstName("John");
        response.setDoctorLastName("Smith");
        response.setIsActive(false);
        response.setIsValid(false);
        return response;
    }
}
