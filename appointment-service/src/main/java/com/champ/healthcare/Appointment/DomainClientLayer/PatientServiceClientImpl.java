package com.champ.healthcare.Appointment.DomainClientLayer;

import com.champ.healthcare.Appointment.utilities.DownstreamServiceException;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class PatientServiceClientImpl implements PatientServiceClient {

    private static final String BASE_PATH = "/api/v1/patients/patient-identifier/{patientId}";

    private final WebClient webClient;

    public PatientServiceClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.patient.url}") String patientServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(patientServiceUrl).build();
    }

    @Override
    public PatientClientResponse getPatientByPatientIdentifier(String patientIdentifier) {
        try {
            return webClient.get()
                    .uri(BASE_PATH, patientIdentifier)
                    .retrieve()
                    .bodyToMono(PatientClientResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Patient not found with patientId: " + patientIdentifier);
        } catch (WebClientResponseException.BadRequest ex) {
            throw new InvalidInputException("Invalid patient identifier: " + patientIdentifier);
        } catch (WebClientResponseException ex) {
            throw new DownstreamServiceException("Patient service returned " + ex.getStatusCode().value() + ".");
        }
    }
}
