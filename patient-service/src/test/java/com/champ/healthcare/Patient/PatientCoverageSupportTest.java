package com.champ.healthcare.Patient;

import com.champ.healthcare.Patient.Domain.*;
import com.champ.healthcare.Patient.PresentationLayer.PatientModelAssembler;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.Patient.utilities.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class PatientCoverageSupportTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.profiles.active=testing"};

        try (var springApplication = mockStatic(SpringApplication.class)) {
            PatientServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(PatientServiceApplication.class, args));
        }
    }

    @Test
    void patientModelAssemblerBuildsEntityAndCollectionLinks() {
        PatientModelAssembler assembler = new PatientModelAssembler();
        PatientResponseDTO patient = patientResponse();

        EntityModel<PatientResponseDTO> model = assembler.toModel(patient);
        CollectionModel<EntityModel<PatientResponseDTO>> collectionModel = assembler.toCollectionModel(
                java.util.List.of(patient)
        );

        assertThat(model.getContent()).isSameAs(patient);
        assertThat(model.getRequiredLink("self").getHref()).contains("/api/v1/patients/1");
        assertThat(model.getRequiredLink("patients").getHref()).contains("/api/v1/patients");
        assertThat(model.getRequiredLink("update").getHref()).contains("/api/v1/patients/1");
        assertThat(model.getRequiredLink("delete").getHref()).contains("/api/v1/patients/1");
        assertThat(collectionModel.getRequiredLink("self").getHref()).contains("/api/v1/patients");
        assertThat(collectionModel.getContent()).hasSize(1);
    }

    @Test
    void globalExceptionHandlerBuildsExpectedResponses() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients");

        ApiErrorResponse notFound = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing patient"),
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

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "patientRequestDTO");
        bindingResult.addError(new FieldError("patientRequestDTO", "fullName", "must not be blank"));
        bindingResult.addError(new ObjectError("patientRequestDTO", "general validation"));
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
        assertThat(notFound.getMessage()).isEqualTo("missing patient");
        assertThat(notFound.getPath()).isEqualTo("/api/v1/patients");
        assertThat(notFound.getTimestamp()).isNotNull();

        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(duplicate.getMessage()).isEqualTo("duplicate email");
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(conflict.getMessage()).isEqualTo("invalid state");
        assertThat(badRequest.getStatus()).isEqualTo(400);
        assertThat(badRequest.getMessage()).isEqualTo("bad request");

        assertThat(validation.getStatus()).isEqualTo(400);
        assertThat(validation.getMessage()).isEqualTo("Validation failed.");
        assertThat(validation.getDetails()).containsExactly("fullName: must not be blank", "general validation");

        assertThat(generic.getStatus()).isEqualTo(500);
        assertThat(generic.getMessage()).isEqualTo("generic failure");
    }

    @SuppressWarnings("unused")
    private void sampleValidationMethod(String value) {
    }

    private PatientResponseDTO patientResponse() {
        return new PatientResponseDTO(
                1L,
                new PatientIdentifier("patient-1"),
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo("john@example.com", "514-555-0101"),
                new Address("123 Main", "Montreal", "Quebec", "H1H1H1", "Canada"),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        );
    }
}
