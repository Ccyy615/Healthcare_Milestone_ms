package com.champ.healthcare.ClinicRoom;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomModelAssembler;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ClinicRoom.utilities.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class ClinicRoomCoverageSupportTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.profiles.active=testing"};

        try (var springApplication = mockStatic(SpringApplication.class)) {
            ClinicRoomServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(ClinicRoomServiceApplication.class, args));
        }
    }

    @Test
    void clinicRoomModelAssemblerBuildsEntityAndCollectionLinks() {
        ClinicRoomModelAssembler assembler = new ClinicRoomModelAssembler();
        ClinicRoomResponseDTO room = roomResponse();

        EntityModel<ClinicRoomResponseDTO> model = assembler.toModel(room);
        CollectionModel<EntityModel<ClinicRoomResponseDTO>> collectionModel = assembler.toCollectionModel(List.of(room));

        assertThat(model.getContent()).isSameAs(room);
        assertThat(model.getRequiredLink("self").getHref()).contains("/api/v1/clinic-rooms/1");
        assertThat(model.getRequiredLink("clinicRooms").getHref()).contains("/api/v1/clinic-rooms");
        assertThat(model.getRequiredLink("update").getHref()).contains("/api/v1/clinic-rooms/1");
        assertThat(model.getRequiredLink("delete").getHref()).contains("/api/v1/clinic-rooms/1");
        assertThat(collectionModel.getRequiredLink("self").getHref()).contains("/api/v1/clinic-rooms");
        assertThat(collectionModel.getContent()).hasSize(1);
    }

    @Test
    void globalExceptionHandlerBuildsExpectedResponses() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clinic-rooms");

        ApiErrorResponse notFound = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing room"),
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

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "clinicRoomRequestDTO");
        bindingResult.addError(new FieldError("clinicRoomRequestDTO", "roomName", "must not be blank"));
        bindingResult.addError(new ObjectError("clinicRoomRequestDTO", "general validation"));
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
        assertThat(notFound.getMessage()).isEqualTo("missing room");
        assertThat(notFound.getPath()).isEqualTo("/api/v1/clinic-rooms");
        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(badRequest.getStatus()).isEqualTo(400);

        assertThat(validation.getStatus()).isEqualTo(400);
        assertThat(validation.getDetails()).containsExactly("roomName: must not be blank", "general validation");

        assertThat(generic.getStatus()).isEqualTo(500);
        assertThat(generic.getMessage()).isEqualTo("generic failure");
    }

    @SuppressWarnings("unused")
    private void sampleValidationMethod(String value) {
    }

    private ClinicRoomResponseDTO roomResponse() {
        return ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }
}
