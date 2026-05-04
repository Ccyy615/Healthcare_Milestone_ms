package com.champ.healthcare.Appointment.DomainClientLayer;

import com.champ.healthcare.Appointment.utilities.DownstreamServiceException;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class DoctorServiceClientImpl implements DoctorServiceClient {

    private static final String BASE_PATH = "/api/v1/doctors/{doctorId}";

    private final WebClient webClient;

    public DoctorServiceClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.doctor.url}") String doctorServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(doctorServiceUrl).build();
    }

    @Override
    public DoctorClientResponse getDoctorByDoctorId(String doctorId) {
        try {
            return webClient.get()
                    .uri(BASE_PATH, doctorId)
                    .retrieve()
                    .bodyToMono(DoctorClientResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Doctor not found with doctorId: " + doctorId);
        } catch (WebClientResponseException.BadRequest ex) {
            throw new InvalidInputException("Invalid doctor identifier: " + doctorId);
        } catch (WebClientResponseException ex) {
            throw new DownstreamServiceException("Doctor service returned " + ex.getStatusCode().value() + ".");
        }
    }
}
