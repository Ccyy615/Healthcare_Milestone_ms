package com.champ.healthcare.Appointment.BusinessLogicLayer;

import com.champ.healthcare.Appointment.DataAccessLayer.AppointmentRepository;
import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomServiceClient;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorServiceClient;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientServiceClient;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.Appointment.utilities.AppointmentConflictException;
import com.champ.healthcare.Appointment.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private DoctorServiceClient doctorServiceClient;

    @Mock
    private ClinicRoomServiceClient clinicRoomServiceClient;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void createAppointmentReturnsEnrichedAppointmentWhenAggregateInvariantPasses() {
        AppointmentRequestDTO request = appointmentRequest();

        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());
        when(appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThan(
                "room-1",
                request.getEndTime(),
                request.getStartTime()
        )).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setAppointmentId(10L);
            return appointment;
        });

        AppointmentResponseDTO response = appointmentService.createAppointment(request);

        assertThat(response.getAppointmentId()).isEqualTo(10L);
        assertThat(response.getPatientFullName()).isEqualTo("Jordan Miles");
        assertThat(response.getPatientEmail()).isEqualTo("jordan@example.com");
        assertThat(response.getDoctorFullName()).isEqualTo("Avery Stone");
        assertThat(response.getRoomName()).isEqualTo("Consultation Room A");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void createAppointmentThrowsConflictWhenRoomIsUnavailable() {
        AppointmentRequestDTO request = appointmentRequest();

        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(
                new ClinicRoomClientResponse(1L, "room-1", "Consultation Room A", "101", "OUT_OF_SERVICE")
        );

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Clinic room room-1 is not available for booking.");
    }

    @Test
    void createAppointmentThrowsInvalidInputWhenPatientIdIsMissing() {
        AppointmentRequestDTO request = appointmentRequest();
        request.setPatientId(" ");

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Patient ID is required.");
    }

    @Test
    void createAppointmentThrowsWhenDoctorIsNotEligible() {
        AppointmentRequestDTO request = appointmentRequest();

        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(
                new DoctorClientResponse("doctor-1", "Avery", "Stone", false, true)
        );
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotEligibleException.class)
                .hasMessage("Doctor doctor-1 must be active and verified before an appointment can be scheduled.");
    }

    @Test
    void createAppointmentThrowsWhenRoomAlreadyBooked() {
        AppointmentRequestDTO request = appointmentRequest();

        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());
        when(appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThan(
                "room-1",
                request.getEndTime(),
                request.getStartTime()
        )).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Cannot create or update appointment: the clinic room is already booked for this time slot.");
    }

    @Test
    void createAppointmentThrowsWhenPatientIsInactive() {
        AppointmentRequestDTO request = appointmentRequest();

        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(
                new PatientClientResponse(
                        1L,
                        new PatientClientResponse.PatientIdentifierResponse("patient-1"),
                        "Jordan Miles",
                        new PatientClientResponse.ContactInfoResponse("jordan@example.com", "514-555-0100"),
                        "INACTIVE"
                )
        );
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Patient patient-1 is not active and cannot be scheduled.");
    }

    @Test
    void createAppointmentThrowsInvalidInputWhenDoctorIdIsMissing() {
        AppointmentRequestDTO request = appointmentRequest();
        request.setDoctorId(" ");

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Doctor ID is required.");
    }

    @Test
    void createAppointmentThrowsInvalidInputWhenRoomIdIsMissing() {
        AppointmentRequestDTO request = appointmentRequest();
        request.setRoomId(" ");

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Room ID is required.");
    }

    @Test
    void createAppointmentThrowsInvalidInputWhenTimeSlotIsInvalid() {
        AppointmentRequestDTO request = appointmentRequest();
        request.setStartTime(null);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Start time and end time are required.");
    }

    @Test
    void getAllAppointmentsReturnsEnrichedAppointments() {
        Appointment appointment = existingAppointment(10L, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findAll()).thenReturn(java.util.List.of(appointment));
        stubAggregateLookups();

        java.util.List<AppointmentResponseDTO> response = appointmentService.getAllAppointments();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getDoctorFullName()).isEqualTo("Avery Stone");
    }

    @Test
    void getAppointmentByIdThrowsWhenMissing() {
        when(appointmentRepository.findByAppointmentId(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 999");
    }

    @Test
    void getAppointmentByIdReturnsEnrichedAppointment() {
        when(appointmentRepository.findByAppointmentId(10L))
                .thenReturn(java.util.Optional.of(existingAppointment(10L, AppointmentStatus.CONFIRMED)));
        stubAggregateLookups();

        AppointmentResponseDTO response = appointmentService.getAppointmentById(10L);

        assertThat(response.getPatientEmail()).isEqualTo("jordan@example.com");
        assertThat(response.getDoctorFullName()).isEqualTo("Avery Stone");
    }

    @Test
    void getAppointmentsByDoctorIdReturnsEnrichedAppointments() {
        when(appointmentRepository.findByDoctorId("doctor-1"))
                .thenReturn(java.util.List.of(existingAppointment(10L, AppointmentStatus.CONFIRMED)));
        stubAggregateLookups();

        assertThat(appointmentService.getAppointmentsByDoctorId("doctor-1")).hasSize(1);
    }

    @Test
    void updateAppointmentUpdatesFieldsAndStatus() {
        AppointmentRequestDTO request = appointmentRequest();
        request.setStatus(AppointmentStatus.CANCELLED);
        Appointment existing = existingAppointment(10L, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findByAppointmentId(10L)).thenReturn(java.util.Optional.of(existing));
        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());
        when(appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThanAndAppointmentIdNot(
                "room-1",
                request.getEndTime(),
                request.getStartTime(),
                10L
        )).thenReturn(false);
        when(appointmentRepository.save(existing)).thenReturn(existing);

        AppointmentResponseDTO response = appointmentService.updateAppointment(10L, request);

        assertThat(existing.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.getRoomName()).isEqualTo("Consultation Room A");
    }

    @Test
    void updateAppointmentThrowsWhenRoomAlreadyBooked() {
        AppointmentRequestDTO request = appointmentRequest();
        Appointment existing = existingAppointment(10L, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findByAppointmentId(10L)).thenReturn(java.util.Optional.of(existing));
        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());
        when(appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThanAndAppointmentIdNot(
                "room-1",
                request.getEndTime(),
                request.getStartTime(),
                10L
        )).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.updateAppointment(10L, request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Cannot create or update appointment: the clinic room is already booked for this time slot.");
    }

    @Test
    void deleteAppointmentDeletesAndReturnsResponse() {
        Appointment appointment = existingAppointment(10L, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByAppointmentId(10L)).thenReturn(java.util.Optional.of(appointment));
        stubAggregateLookups();

        AppointmentResponseDTO response = appointmentService.deleteAppointment(10L);

        assertThat(response.getAppointmentId()).isEqualTo(10L);
        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void deleteAppointmentThrowsWhenMissing() {
        when(appointmentRepository.findByAppointmentId(77L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 77");
    }

    @Test
    void completeAppointmentThrowsConflictWhenAppointmentAlreadyCancelled() {
        Appointment appointment = existingAppointment(15L, AppointmentStatus.CANCELLED);

        when(appointmentRepository.findByAppointmentId(15L)).thenReturn(java.util.Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.completeAppointment(15L))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("A CANCELLED appointment cannot be completed.");
    }

    @Test
    void completeAppointmentThrowsWhenAlreadyCompleted() {
        Appointment appointment = existingAppointment(15L, AppointmentStatus.COMPLETED);

        when(appointmentRepository.findByAppointmentId(15L)).thenReturn(java.util.Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.completeAppointment(15L))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Appointment is already COMPLETED.");
    }

    @Test
    void cancelAppointmentThrowsWhenCompleted() {
        Appointment appointment = existingAppointment(12L, AppointmentStatus.COMPLETED);

        when(appointmentRepository.findByAppointmentId(12L)).thenReturn(java.util.Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(12L))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("A COMPLETED appointment cannot be cancelled.");
    }

    @Test
    void cancelAppointmentReturnsUpdatedStatus() {
        Appointment appointment = existingAppointment(12L, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findByAppointmentId(12L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        stubAggregateLookups();

        AppointmentResponseDTO response = appointmentService.cancelAppointment(12L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void completeAppointmentReturnsUpdatedStatus() {
        Appointment appointment = existingAppointment(15L, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findByAppointmentId(15L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        stubAggregateLookups();

        AppointmentResponseDTO response = appointmentService.completeAppointment(15L);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void completeAppointmentThrowsWhenMissing() {
        when(appointmentRepository.findByAppointmentId(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> appointmentService.completeAppointment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 99");
    }

    @Test
    void cancelAppointmentThrowsWhenMissing() {
        when(appointmentRepository.findByAppointmentId(98L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(98L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 98");
    }

    private AppointmentRequestDTO appointmentRequest() {
        return AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();
    }

    private PatientClientResponse activePatient() {
        return new PatientClientResponse(
                1L,
                new PatientClientResponse.PatientIdentifierResponse("patient-1"),
                "Jordan Miles",
                new PatientClientResponse.ContactInfoResponse("jordan@example.com", "514-555-0100"),
                "ACTIVE"
        );
    }

    private DoctorClientResponse eligibleDoctor() {
        return new DoctorClientResponse("doctor-1", "Avery", "Stone", true, true);
    }

    private ClinicRoomClientResponse availableRoom() {
        return new ClinicRoomClientResponse(1L, "room-1", "Consultation Room A", "101", "AVAILABLE");
    }

    private Appointment existingAppointment(Long appointmentId, AppointmentStatus status) {
        return Appointment.builder()
                .appointmentId(appointmentId)
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .status(status)
                .timeSlot(new TimeSlot(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)))
                .createdAt(LocalDateTime.now())
                .description("Annual checkup")
                .build();
    }

    private void stubAggregateLookups() {
        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(activePatient());
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(eligibleDoctor());
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(availableRoom());
    }
}
