package com.champ.healthcare.Appointment.DataAccessLayer;

import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testing")
@Transactional
class AppointmentRepositoryIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void findByAppointmentIdReturnsPersistedAppointment() {
        Appointment saved = appointmentRepository.save(appointment("room-1", 9, 10));

        assertThat(appointmentRepository.findByAppointmentId(saved.getAppointmentId()))
                .isPresent()
                .get()
                .extracting(Appointment::getDoctorId)
                .isEqualTo("doctor-1");
    }

    @Test
    void overlapQueryReturnsTrueWhenRoomBookingConflicts() {
        Appointment saved = appointmentRepository.save(appointment("room-1", 9, 10));

        boolean overlappingBookingExists =
                appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThan(
                        "room-1",
                        LocalDateTime.of(2026, 5, 10, 9, 30),
                        LocalDateTime.of(2026, 5, 10, 8, 30)
                );

        boolean overlapIgnoringCurrentRecord =
                appointmentRepository.existsByRoomIdAndTimeSlot_StartTimeLessThanAndTimeSlot_EndTimeGreaterThanAndAppointmentIdNot(
                        "room-1",
                        LocalDateTime.of(2026, 5, 10, 9, 30),
                        LocalDateTime.of(2026, 5, 10, 8, 30),
                        saved.getAppointmentId()
                );

        assertThat(overlappingBookingExists).isTrue();
        assertThat(overlapIgnoringCurrentRecord).isFalse();
    }

    private Appointment appointment(String roomId, int startHour, int endHour) {
        return Appointment.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId(roomId)
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .timeSlot(new TimeSlot(
                        LocalDateTime.of(2026, 5, 10, startHour, 0),
                        LocalDateTime.of(2026, 5, 10, endHour, 0)
                ))
                .description("Integration test appointment")
                .build();
    }
}
