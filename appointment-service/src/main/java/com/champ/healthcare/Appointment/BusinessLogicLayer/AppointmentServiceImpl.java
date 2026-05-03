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
import com.champ.healthcare.Appointment.Mapper.AppointmentMapper;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.Appointment.utilities.AppointmentConflictException;
import com.champ.healthcare.Appointment.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientServiceClient patientServiceClient;
    private final DoctorServiceClient doctorServiceClient;
    private final ClinicRoomServiceClient clinicRoomServiceClient;

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        return buildResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDoctorId(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        validateRequest(dto);
        PatientClientResponse patient = getRequiredPatient(dto.getPatientId());
        DoctorClientResponse doctor = getRequiredDoctor(dto.getDoctorId());
        ClinicRoomClientResponse room = getRequiredRoom(dto.getRoomId());

        validateAggregateInvariant(patient, doctor, room);
        throwIfRoomDoubleBooked(dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), null);

        Appointment appointment = AppointmentMapper.toEntity(dto);
        appointment.validateTimeSlot();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return buildResponse(savedAppointment, patient, doctor, room);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long appointmentId, AppointmentRequestDTO dto) {
        validateRequest(dto);

        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        PatientClientResponse patient = getRequiredPatient(dto.getPatientId());
        DoctorClientResponse doctor = getRequiredDoctor(dto.getDoctorId());
        ClinicRoomClientResponse room = getRequiredRoom(dto.getRoomId());

        validateAggregateInvariant(patient, doctor, room);
        throwIfRoomDoubleBooked(dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), appointmentId);

        appointment.setPatientId(dto.getPatientId());
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setRoomId(dto.getRoomId());
        appointment.setTimeSlot(new TimeSlot(dto.getStartTime(), dto.getEndTime()));
        appointment.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            appointment.setStatus(dto.getStatus());
        }

        appointment.validateTimeSlot();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return buildResponse(savedAppointment, patient, doctor, room);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        appointmentRepository.delete(appointment);
        return buildResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("A CANCELLED appointment cannot be completed.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Appointment is already COMPLETED.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return buildResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("A COMPLETED appointment cannot be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return buildResponse(appointmentRepository.save(appointment));
    }

    private void throwIfRoomDoubleBooked(String roomId,
                                         java.time.LocalDateTime startTime,
                                         java.time.LocalDateTime endTime,
                                         Long appointmentIdToExclude) {
        boolean overlap;

        if (appointmentIdToExclude == null) {
            overlap = appointmentRepository
                    .existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThan(
                            roomId, endTime, startTime
                    );
        } else {
            overlap = appointmentRepository
                    .existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThanAndAppointmentIdNot(
                            roomId, endTime, startTime, appointmentIdToExclude
                    );
        }

        if (overlap) {
            throw new AppointmentConflictException(
                    "Cannot create or update appointment: the clinic room is already booked for this time slot."
            );
        }
    }

    private void validateRequest(AppointmentRequestDTO dto) {
        if (dto.getPatientId() == null || dto.getPatientId().isBlank()) {
            throw new InvalidInputException("Patient ID is required.");
        }

        if (dto.getDoctorId() == null || dto.getDoctorId().isBlank()) {
            throw new InvalidInputException("Doctor ID is required.");
        }

        if (dto.getRoomId() == null || dto.getRoomId().isBlank()) {
            throw new InvalidInputException("Room ID is required.");
        }

        TimeSlot timeSlot = new TimeSlot(dto.getStartTime(), dto.getEndTime());
        try {
            timeSlot.validate();
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }

    private PatientClientResponse getRequiredPatient(String patientId) {
        return patientServiceClient.getPatientByPatientIdentifier(patientId);
    }

    private DoctorClientResponse getRequiredDoctor(String doctorId) {
        return doctorServiceClient.getDoctorByDoctorId(doctorId);
    }

    private ClinicRoomClientResponse getRequiredRoom(String roomId) {
        return clinicRoomServiceClient.getRoomByRoomId(roomId);
    }

    private void validateAggregateInvariant(
            PatientClientResponse patient,
            DoctorClientResponse doctor,
            ClinicRoomClientResponse room
    ) {
        if (!"ACTIVE".equalsIgnoreCase(patient.status())) {
            throw new AppointmentConflictException(
                    "Patient " + patient.patientIdentifier() + " is not active and cannot be scheduled."
            );
        }

        if (!Boolean.TRUE.equals(doctor.isActive()) || !Boolean.TRUE.equals(doctor.isValid())) {
            throw new DoctorNotEligibleException(
                    "Doctor " + doctor.doctorId() + " must be active and verified before an appointment can be scheduled."
            );
        }

        if (!"AVAILABLE".equalsIgnoreCase(room.roomStatus())) {
            throw new AppointmentConflictException(
                    "Clinic room " + room.roomId() + " is not available for booking."
            );
        }
    }

    private AppointmentResponseDTO buildResponse(Appointment appointment) {
        PatientClientResponse patient = getRequiredPatient(appointment.getPatientId());
        DoctorClientResponse doctor = getRequiredDoctor(appointment.getDoctorId());
        ClinicRoomClientResponse room = getRequiredRoom(appointment.getRoomId());
        return buildResponse(appointment, patient, doctor, room);
    }

    private AppointmentResponseDTO buildResponse(
            Appointment appointment,
            PatientClientResponse patient,
            DoctorClientResponse doctor,
            ClinicRoomClientResponse room
    ) {
        AppointmentResponseDTO response = AppointmentMapper.toResponseDTO(appointment);
        response.setPatientFullName(patient.fullName());
        response.setPatientEmail(patient.email());
        response.setDoctorFullName(doctor.fullName());
        response.setRoomName(room.roomName());
        response.setRoomNumber(room.roomNumber());
        response.setRoomStatus(room.roomStatus());
        return response;
    }
}
