package com.champ.healthcare.Doctor;

import com.champ.healthcare.Doctor.utilities.ApiErrorResponse;
import com.champ.healthcare.Doctor.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Doctor.utilities.DuplicateEmailException;
import com.champ.healthcare.Doctor.utilities.GlobalExceptionHandler;
import com.champ.healthcare.Doctor.utilities.ResourceNotFoundException;
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
        ApiErrorResponse notEligible = handler.handleDoctorNotEligible(
                new DoctorNotEligibleException("doctor not eligible"),
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
                new RuntimeException("generic failure"),
                request
        ).getBody();

        assertThat(notFound.getStatus()).isEqualTo(404);
        assertThat(notFound.getMessage()).isEqualTo("missing doctor");
        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(notEligible.getStatus()).isEqualTo(400);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(badRequest.getStatus()).isEqualTo(400);
        assertThat(validation.getDetails())
                .containsExactly("doctorFirstName: must not be blank", "general validation");
        assertThat(generic.getStatus()).isEqualTo(500);
    }

    @SuppressWarnings("unused")
    private void sampleValidationMethod(String value) {
    }
}
