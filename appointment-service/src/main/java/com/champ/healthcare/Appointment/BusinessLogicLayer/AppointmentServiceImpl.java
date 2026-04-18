package com.champ.healthcare.Appointment.BusinessLogicLayer;

import com.champ.healthcare.Appointment.DataAccessLayer.AppointmentRepository;
import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import com.champ.healthcare.Appointment.Mapper.AppointmentMapper;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
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

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        return AppointmentMapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDoctorId(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(AppointmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        validateRequest(dto);
        throwIfRoomDoubleBooked(dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), null);

        Appointment appointment = AppointmentMapper.toEntity(dto);
        appointment.validateTimeSlot();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponseDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long appointmentId, AppointmentRequestDTO dto) {
        validateRequest(dto);

        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

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
        return AppointmentMapper.toResponseDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        appointmentRepository.delete(appointment);
        return AppointmentMapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("A CANCELLED appointment cannot be completed.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Appointment is already COMPLETED.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId
                ));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("A COMPLETED appointment cannot be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
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
            throw new IllegalStateException(
                    "Cannot create or update appointment: the clinic room is already booked for this time slot."
            );
        }
    }

    private void validateRequest(AppointmentRequestDTO dto) {
        if (dto.getPatientId() == null || dto.getPatientId().isBlank()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }

        if (dto.getDoctorId() == null || dto.getDoctorId().isBlank()) {
            throw new IllegalArgumentException("Doctor ID is required.");
        }

        if (dto.getRoomId() == null || dto.getRoomId().isBlank()) {
            throw new IllegalArgumentException("Room ID is required.");
        }

        TimeSlot timeSlot = new TimeSlot(dto.getStartTime(), dto.getEndTime());
        timeSlot.validate();
    }
}