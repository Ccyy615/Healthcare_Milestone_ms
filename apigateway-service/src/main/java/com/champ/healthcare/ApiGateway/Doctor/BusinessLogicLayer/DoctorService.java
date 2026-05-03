package com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.*;
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
public class DoctorService {

    private final WebClient webClient;
    private static final String BASE_PATH = "/api/v1/doctors";

    public DoctorService(
            WebClient.Builder webClientBuilder,
            @Value("${services.doctor.url}") String doctorServiceUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(doctorServiceUrl)
                .build();
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        log.info("Fetching all doctors");

        return execute(() -> webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(DoctorResponseDTO.class)
                .collectList()
                .block());
    }

    public DoctorResponseDTO getDoctorById(String doctorId) {
        log.info("Fetching doctor with id: {}", doctorId);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/" + doctorId)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        log.info("Creating doctor");

        return execute(() -> webClient.post()
                .uri(BASE_PATH)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO updateDoctor(String doctorId, DoctorRequestDTO requestDTO) {
        log.info("Updating doctor with id: {}", doctorId);

        return execute(() -> webClient.put()
                .uri(BASE_PATH + "/" + doctorId)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public List<DoctorResponseDTO> getActiveDoctors() {
        log.info("Fetching active doctors");

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/active")
                .retrieve()
                .bodyToFlux(DoctorResponseDTO.class)
                .collectList()
                .block());
    }

    public List<DoctorResponseDTO> getActiveDoctorBySpeciality(String specialityName) {
        log.info("Fetching active doctors by speciality: {}", specialityName);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/active/speciality/" + specialityName)
                .retrieve()
                .bodyToFlux(DoctorResponseDTO.class)
                .collectList()
                .block());
    }

    public DoctorResponseDTO activateDoctor(String doctorId) {
        log.info("Activating doctor with id: {}", doctorId);

        return execute(() -> webClient.post()
                .uri(BASE_PATH + "/" + doctorId + "/activate")
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO deactivateDoctor(String doctorId) {
        log.info("Deactivating doctor with id: {}", doctorId);

        return execute(() -> webClient.post()
                .uri(BASE_PATH + "/" + doctorId + "/deactivate")
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO updateDoctorActivation(String doctorId, Boolean active) {
        log.info("Updating activation for doctor with id: {}", doctorId);

        return execute(() -> webClient.patch()
                .uri(BASE_PATH + "/" + doctorId + "/activation")
                .bodyValue(new DoctorActivationPatchDTO(active))
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO addSpeciality(String doctorId, SpecialityRequestDTO specialityDTO) {
        log.info("Adding speciality to doctor with id: {}", doctorId);

        return execute(() -> webClient.post()
                .uri(BASE_PATH + "/" + doctorId + "/speciality")
                .bodyValue(specialityDTO)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO removeSpeciality(String doctorId, String specialityName) {
        log.info("Removing speciality {} from doctor {}", specialityName, doctorId);

        return execute(() -> webClient.delete()
                .uri(BASE_PATH + "/" + doctorId + "/speciality/" + specialityName)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public DoctorResponseDTO addLicense(String doctorId, LicenseRequestDTO requestDTO) {
        log.info("Adding license to doctor with id: {}", doctorId);

        return execute(() -> webClient.post()
                .uri(BASE_PATH + "/" + doctorId + "/license")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(DoctorResponseDTO.class)
                .block());
    }

    public void deleteDoctor(String doctorId) {
        log.info("Deleting doctor with id: {}", doctorId);

        execute(() -> webClient.delete()
                .uri(BASE_PATH + "/" + doctorId)
                .retrieve()
                .bodyToMono(Void.class)
                .block());
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebClientResponseException ex) {
            throw WebClientErrorMapper.map("Doctor service", ex);
        }
    }
}
