package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.BusinessLogicLayer.MedicalNoteService;
import com.champ.healthcare.Appointment.Domain.NoteType;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalNoteControllerTest {

    @Mock
    private MedicalNoteService medicalNoteService;

    private MedicalNoteController medicalNoteController;

    @BeforeEach
    void setUp() {
        medicalNoteController = new MedicalNoteController(medicalNoteService);
    }

    @Test
    void getAllNotesReturnsOk() {
        when(medicalNoteService.getAllNotes()).thenReturn(List.of(noteResponse()));

        assertThat(medicalNoteController.getAllNotes().getBody()).hasSize(1);
    }

    @Test
    void getNoteByIdReturnsOk() {
        when(medicalNoteService.getNoteById(1L)).thenReturn(noteResponse());

        assertThat(medicalNoteController.getNoteById(1L).getBody().getNoteText()).isEqualTo("Consultation summary");
    }

    @Test
    void createNoteReturnsOk() {
        MedicalNoteRequestDTO requestDTO = noteRequest();
        when(medicalNoteService.createNote(requestDTO)).thenReturn(noteResponse());

        assertThat(medicalNoteController.createNote(requestDTO).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateNoteReturnsOk() {
        MedicalNoteRequestDTO requestDTO = noteRequest();
        when(medicalNoteService.updateNote(1L, requestDTO)).thenReturn(noteResponse());

        assertThat(medicalNoteController.updateNote(1L, requestDTO).getBody().getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void deleteNoteReturnsOk() {
        when(medicalNoteService.deleteNote(1L)).thenReturn(noteResponse());

        assertThat(medicalNoteController.deleteNote(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getNoteByIdPropagatesNegativePath() {
        when(medicalNoteService.getNoteById(404L)).thenThrow(new ResourceNotFoundException("Note not found"));

        assertThatThrownBy(() -> medicalNoteController.getNoteById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Note not found");
    }

    private MedicalNoteRequestDTO noteRequest() {
        return MedicalNoteRequestDTO.builder()
                .appointmentId(10L)
                .doctorId("doctor-1")
                .patientId("patient-1")
                .noteText("Consultation summary")
                .noteType(NoteType.CONSULTATION)
                .build();
    }

    private MedicalNoteResponseDTO noteResponse() {
        return MedicalNoteResponseDTO.builder()
                .noteId(1L)
                .appointmentId(10L)
                .doctorId("doctor-1")
                .patientId("patient-1")
                .noteText("Consultation summary")
                .createdAt(LocalDateTime.of(2026, 5, 1, 9, 0))
                .lastUpdatedAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .noteType(NoteType.CONSULTATION)
                .build();
    }
}
