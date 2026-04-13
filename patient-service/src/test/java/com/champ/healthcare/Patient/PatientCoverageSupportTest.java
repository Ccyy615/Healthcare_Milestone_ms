package com.champ.healthcare.Patient;

import com.champ.healthcare.Patient.utilities.ApiErrorResponse;
import com.champ.healthcare.Patient.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Patient.utilities.DuplicateEmailException;
import com.champ.healthcare.Patient.utilities.GlobalExceptionHandler;
import com.champ.healthcare.Patient.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

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
                new RuntimeException("generic failure"),
                request
        ).getBody();

        assertThat(notFound.getStatus()).isEqualTo(404);
        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(badRequest.getStatus()).isEqualTo(400);
        assertThat(validation.getDetails()).containsExactly("fullName: must not be blank", "general validation");
        assertThat(generic.getStatus()).isEqualTo(500);
    }

    @Test
    void unusedDoctorNotEligibleExceptionRetainsItsMessage() {
        DoctorNotEligibleException exception = new DoctorNotEligibleException("doctor not eligible");

        assertThat(exception).hasMessage("doctor not eligible");
    }

    @SuppressWarnings("unused")
    private void sampleValidationMethod(String value) {
    }
}
