package com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientStatusPatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final WebClient webClient;
    private static final String BASE_PATH = "/api/v1/patients";

    public PatientServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://patient-service")
                .build();
    }

    @Override
    public List<PatientResponseDTO> getAllPatients() {
        log.info("Fetching all patients");

        return webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(PatientResponseDTO.class)
                .collectList()
                .block();
    }

    @Override
    public PatientResponseDTO getPatientById(Long id) {
        log.info("Fetching patient with id: {}", id);

        return webClient.get()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block();
    }

    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        log.info("Creating patient");

        return webClient.post()
                .uri(BASE_PATH)
                .bodyValue(patientRequestDTO)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block();
    }

    @Override
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO) {
        log.info("Updating patient with id: {}", id);

        return webClient.put()
                .uri(BASE_PATH + "/" + id)
                .bodyValue(patientRequestDTO)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block();
    }

    @Override
    public PatientResponseDTO updatePatientStatus(Long id, PatientStatusPatchDTO status) {
        log.info("Updating patient status with id: {}", id);

        return webClient.patch()
                .uri(BASE_PATH + "/" + id + "/status")
                .bodyValue(status)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block();
    }

    @Override
    public PatientResponseDTO deletePatientById(Long id) {
        log.info("Deleting patient with id: {}", id);

        return webClient.delete()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(PatientResponseDTO.class)
                .block();
    }
}