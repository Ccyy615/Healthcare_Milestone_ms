package com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.ApiGateway.utilities.WebClientErrorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final String BASE_PATH = "/api/v1/appointments";

    private final WebClient webClient;

    public AppointmentServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.appointment.url}") String appointmentServiceUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(appointmentServiceUrl)
                .build();
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        return execute(() -> webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(AppointmentResponseDTO.class)
                .collectList()
                .block());
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(Long appointmentId) {
        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/" + appointmentId)
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByDoctorId(String doctorId) {
        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/doctor/" + doctorId)
                .retrieve()
                .bodyToFlux(AppointmentResponseDTO.class)
                .collectList()
                .block());
    }

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO appointmentRequestDTO) {
        return execute(() -> webClient.post()
                .uri(BASE_PATH)
                .bodyValue(appointmentRequestDTO)
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    @Override
    public AppointmentResponseDTO updateAppointment(Long appointmentId, AppointmentRequestDTO appointmentRequestDTO) {
        return execute(() -> webClient.put()
                .uri(BASE_PATH + "/" + appointmentId)
                .bodyValue(appointmentRequestDTO)
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    @Override
    public AppointmentResponseDTO deleteAppointment(Long appointmentId) {
        return execute(() -> webClient.delete()
                .uri(BASE_PATH + "/" + appointmentId)
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    @Override
    public AppointmentResponseDTO completeAppointment(Long appointmentId) {
        return execute(() -> webClient.patch()
                .uri(BASE_PATH + "/" + appointmentId + "/complete")
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    @Override
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        return execute(() -> webClient.patch()
                .uri(BASE_PATH + "/" + appointmentId + "/cancel")
                .retrieve()
                .bodyToMono(AppointmentResponseDTO.class)
                .block());
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebClientResponseException ex) {
            throw WebClientErrorMapper.map("Appointment service", ex);
        }
    }
}
