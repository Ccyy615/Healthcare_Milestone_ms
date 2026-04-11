package com.champ.healthcare.Appointment.Mapper;

import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Patient.Domain.PatientIdentifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {

    public static AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentResponseDTO.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId() != null ? appointment.getPatientId().getPatientId() : null)
                .doctorId(appointment.getDoctorId() != null ? appointment.getDoctorId().getDoctorId() : null)
                .roomId(appointment.getRoomId() != null ? appointment.getRoomId().getRoomId() : null)
                .status(appointment.getStatus() != null ? appointment.getStatus().name() : null)
                .createdAt(appointment.getCreatedAt())
                .startTime(appointment.getTimeSlot() != null ? appointment.getTimeSlot().getStartTime() : null)
                .endTime(appointment.getTimeSlot() != null ? appointment.getTimeSlot().getEndTime() : null)
                .description(appointment.getDescription())
                .build();
    }

    public static Appointment toEntity(AppointmentRequestDTO dto) {
        AppointmentStatus status = dto.getStatus() != null
                ? dto.getStatus()
                : AppointmentStatus.CONFIRMED;

        return Appointment.builder()
                .patientId(new PatientIdentifier(dto.getPatientId()))
                .doctorId(new DoctorIdentifier(dto.getDoctorId()))
                .roomId(new ClinicRoomIdentifier(dto.getRoomId()))
                .status(status)
                .createdAt(LocalDateTime.now())
                .timeSlot(new TimeSlot(dto.getStartTime(), dto.getEndTime()))
                .description(dto.getDescription())
                .build();
    }

    public static List<AppointmentResponseDTO> toResponseDTOList(List<Appointment> appointments) {
        return appointments.stream()
                .map(AppointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}