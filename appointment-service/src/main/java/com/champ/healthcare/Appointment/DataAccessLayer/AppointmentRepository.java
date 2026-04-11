package com.champ.healthcare.Appointment.DataAccessLayer;

import com.champ.healthcare.Appointment.Domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentId(Long appointmentId);

    List<Appointment> findByDoctorId_DoctorId(String doctorId);

    boolean existsByRoomId_RoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThan(
            String roomId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    boolean existsByRoomId_RoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThanAndAppointmentIdNot(
            String roomId,
            LocalDateTime endTime,
            LocalDateTime startTime,
            Long appointmentId
    );
}