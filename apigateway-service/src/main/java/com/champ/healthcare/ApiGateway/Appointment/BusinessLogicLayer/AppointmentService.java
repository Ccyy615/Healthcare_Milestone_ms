package com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {

    List<AppointmentResponseDTO> getAllAppointments();

    AppointmentResponseDTO getAppointmentById(Long appointmentId);

    List<AppointmentResponseDTO> getAppointmentsByDoctorId(String doctorId);

    AppointmentResponseDTO createAppointment(AppointmentRequestDTO appointmentRequestDTO);

    AppointmentResponseDTO updateAppointment(Long appointmentId, AppointmentRequestDTO appointmentRequestDTO);

    AppointmentResponseDTO deleteAppointment(Long appointmentId);

    AppointmentResponseDTO completeAppointment(Long appointmentId);

    AppointmentResponseDTO cancelAppointment(Long appointmentId);
}
