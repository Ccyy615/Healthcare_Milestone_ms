package com.champ.healthcare.Doctor;

import com.champ.healthcare.Doctor.Domain.*;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorModelAssembler;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.Doctor.utilities.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class DoctorCoverageSupportTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.profiles.active=testing"};

        try (var springApplication = mockStatic(SpringApplication.class)) {
            DoctorServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(DoctorServiceApplication.class, args));
        }
    }

    @Test
    void doctorModelAssemblerBuildsEntityAndCollectionLinks() {
        DoctorModelAssembler assembler = new DoctorModelAssembler();
        DoctorResponseDTO doctor = doctorResponse();

        EntityModel<DoctorResponseDTO> model = assembler.toModel(doctor);
        CollectionModel<EntityModel<DoctorResponseDTO>> collectionModel = assembler.toCollectionModel(List.of(doctor));

        assertThat(model.getContent()).isSameAs(doctor);
        assertThat(model.getRequiredLink("self").getHref()).contains("/api/v1/doctors/doctor-1");
        assertThat(model.getRequiredLink("doctors").getHref()).contains("/api/v1/doctors");
        assertThat(model.getRequiredLink("update").getHref()).contains("/api/v1/doctors/doctor-1");
        assertThat(model.getRequiredLink("delete").getHref()).contains("/api/v1/doctors/doctor-1");
        assertThat(model.getRequiredLink("activate").getHref()).contains("/api/v1/doctors/doctor-1/activate");
        assertThat(model.getRequiredLink("deactivate").getHref()).contains("/api/v1/doctors/doctor-1/deactivate");
        assertThat(collectionModel.getRequiredLink("self").getHref()).contains("/api/v1/doctors");
        assertThat(collectionModel.getContent()).hasSize(1);
    }

    @Test
    void globalExceptionHandlerBuildsExpectedResponses() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/doctors");

        ApiErrorResponse notFound = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing doctor"),
                request
        ).getBody();
        ApiErrorResponse duplicate = handler.handleDuplicateEmail(
                new DuplicateEmailException("duplicate email"),
                request
        ).getBody();
        ApiErrorResponse conflict = handler.handleIllegalState(
                new IllegalStateException("invalid state"),
                request
        ).getBody();
        ApiErrorResponse badRequest = handler.handleIllegalArgument(
                new IllegalArgumentException("bad request"),
                request
        ).getBody();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "doctorRequestDTO");
        bindingResult.addError(new FieldError("doctorRequestDTO", "doctorFirstName", "must not be blank"));
        bindingResult.addError(new ObjectError("doctorRequestDTO", "general validation"));
        Method method = getClass().getDeclaredMethod("sampleValidationMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException validationException =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ApiErrorResponse validation = handler.handleValidationErrors(validationException, request).getBody();
        ApiErrorResponse generic = handler.handleGenericException(
                new DoctorNotEligibleException("generic failure"),
                request
        ).getBody();

        assertThat(notFound.getStatus()).isEqualTo(404);
        assertThat(notFound.getMessage()).isEqualTo("missing doctor");
        assertThat(notFound.getPath()).isEqualTo("/api/v1/doctors");
        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(badRequest.getStatus()).isEqualTo(400);

        assertThat(validation.getStatus()).isEqualTo(400);
        assertThat(validation.getDetails())
                .containsExactly("doctorFirstName: must not be blank", "general validation");

        assertThat(generic.getStatus()).isEqualTo(500);
        assertThat(generic.getMessage()).isEqualTo("generic failure");
    }

    @SuppressWarnings("unused")
    private void sampleValidationMethod(String value) {
    }

    private DoctorResponseDTO doctorResponse() {
        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId("doctor-1");
        response.setDoctorFirstName("John");
        response.setDoctorLastName("Smith");
        response.setIsActive(true);
        response.setIsValid(true);
        response.setWorkZone(new WorkZone("Montreal", "Quebec"));
        response.setSpeciality(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT)));
        response.setLicense(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));
        return response;
    }
}
