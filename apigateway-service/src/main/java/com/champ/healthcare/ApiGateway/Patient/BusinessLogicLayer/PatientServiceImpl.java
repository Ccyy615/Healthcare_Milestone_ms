package com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientStatusPatchDTO;
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
public class PatientServiceImpl implements PatientService {

    private final WebClient webClient;
    private static final String BASE_PATH = "/api/v1/patients";

    public PatientServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.patient.url}") String patientServiceUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(patientServiceUrl)
                .build();
    }

    @Override
    public List<PatientResponseDTO> getAllPatients() {
        log.info("Fetching all patients");

        return execute(() -> webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(PatientResponseDTO.class)
                .collectList()
                .block());
    }

    @Override
    public PatientResponseDTO getPatientById(Long id) {
        log.info("Fetching patient with id: {}", id);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    @Override
    public PatientResponseDTO getPatientByPatientIdentifier(String patientId) {
        log.info("Fetching patient with patient identifier: {}", patientId);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/patient-identifier/" + patientId)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        log.info("Creating patient");

        return execute(() -> webClient.post()
                .uri(BASE_PATH)
                .bodyValue(patientRequestDTO)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    @Override
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO) {
        log.info("Updating patient with id: {}", id);

        return execute(() -> webClient.put()
                .uri(BASE_PATH + "/" + id)
                .bodyValue(patientRequestDTO)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    @Override
    public PatientResponseDTO updatePatientStatus(Long id, PatientStatusPatchDTO status) {
        log.info("Updating patient status with id: {}", id);

        return execute(() -> webClient.patch()
                .uri(BASE_PATH + "/" + id + "/status")
                .bodyValue(status)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    @Override
    public PatientResponseDTO deletePatientById(Long id) {
        log.info("Deleting patient with id: {}", id);

        return execute(() -> webClient.delete()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block());
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebClientResponseException ex) {
            throw WebClientErrorMapper.map("Patient service", ex);
        }
    }
}
