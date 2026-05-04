package com.champ.healthcare.Appointment.BusinessLogicLayer;

import com.champ.healthcare.Appointment.DataAccessLayer.AppointmentRepository;
import com.champ.healthcare.Appointment.DataAccessLayer.MedicalNoteRepository;
import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.MedicalNote;
import com.champ.healthcare.Appointment.Domain.NoteType;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteResponseDTO;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalNoteServiceTest {

    @Mock
    private MedicalNoteRepository medicalNoteRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalNoteService medicalNoteService;

    @Test
    void getAllNotesReturnsMappedResponses() {
        when(medicalNoteRepository.findAll()).thenReturn(List.of(existingNote(1L, existingAppointment(10L))));

        List<MedicalNoteResponseDTO> response = medicalNoteService.getAllNotes();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void getNoteByIdThrowsWhenMissing() {
        when(medicalNoteRepository.findByNoteId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalNoteService.getNoteById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Note not found with ID: 99");
    }

    @Test
    void getNoteByIdReturnsMappedResponse() {
        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.of(existingNote(1L, existingAppointment(10L))));

        MedicalNoteResponseDTO response = medicalNoteService.getNoteById(1L);

        assertThat(response.getDoctorId()).isEqualTo("doctor-1");
    }

    @Test
    void createNoteUsesAppointmentAndSetsTimestamps() {
        Appointment appointment = existingAppointment(10L);
        MedicalNoteRequestDTO request = noteRequest(10L);

        when(appointmentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(appointment));
        when(medicalNoteRepository.save(org.mockito.ArgumentMatchers.any(MedicalNote.class)))
                .thenAnswer(invocation -> {
                    MedicalNote note = invocation.getArgument(0);
                    note.setNoteId(1L);
                    return note;
                });

        MedicalNoteResponseDTO response = medicalNoteService.createNote(request);

        assertThat(response.getNoteId()).isEqualTo(1L);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getLastUpdatedAt()).isNotNull();
    }

    @Test
    void createNoteThrowsWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalNoteService.createNote(noteRequest(10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 10");
    }

    @Test
    void updateNoteReassignsAppointmentWhenAppointmentIdChanges() {
        Appointment oldAppointment = existingAppointment(10L);
        Appointment newAppointment = existingAppointment(11L);
        MedicalNote existingNote = existingNote(1L, oldAppointment);
        MedicalNoteRequestDTO request = noteRequest(11L);

        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.of(existingNote));
        when(appointmentRepository.findByAppointmentId(11L)).thenReturn(Optional.of(newAppointment));
        when(medicalNoteRepository.save(existingNote)).thenReturn(existingNote);

        MedicalNoteResponseDTO response = medicalNoteService.updateNote(1L, request);

        assertThat(existingNote.getAppointment().getAppointmentId()).isEqualTo(11L);
        assertThat(response.getAppointmentId()).isEqualTo(11L);
    }

    @Test
    void updateNoteKeepsExistingAppointmentWhenAppointmentIdDoesNotChange() {
        Appointment appointment = existingAppointment(10L);
        MedicalNote existingNote = existingNote(1L, appointment);
        MedicalNoteRequestDTO request = noteRequest(10L);

        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.of(existingNote));
        when(medicalNoteRepository.save(existingNote)).thenReturn(existingNote);

        MedicalNoteResponseDTO response = medicalNoteService.updateNote(1L, request);

        assertThat(existingNote.getAppointment().getAppointmentId()).isEqualTo(10L);
        assertThat(response.getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void updateNoteThrowsWhenNoteDoesNotExist() {
        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalNoteService.updateNote(1L, noteRequest(10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Note not found with ID: 1");
    }

    @Test
    void updateNoteThrowsWhenNewAppointmentDoesNotExist() {
        MedicalNote existingNote = existingNote(1L, existingAppointment(10L));

        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.of(existingNote));
        when(appointmentRepository.findByAppointmentId(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalNoteService.updateNote(1L, noteRequest(11L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 11");
    }

    @Test
    void deleteNoteDeletesAndReturnsResponse() {
        MedicalNote note = existingNote(1L, existingAppointment(10L));
        when(medicalNoteRepository.findByNoteId(1L)).thenReturn(Optional.of(note));

        MedicalNoteResponseDTO response = medicalNoteService.deleteNote(1L);

        assertThat(response.getNoteId()).isEqualTo(1L);
        verify(medicalNoteRepository).delete(note);
    }

    @Test
    void deleteNoteThrowsWhenMissing() {
        when(medicalNoteRepository.findByNoteId(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalNoteService.deleteNote(88L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Note not found with ID: 88");
    }

    private Appointment existingAppointment(Long appointmentId) {
        return Appointment.builder()
                .appointmentId(appointmentId)
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .timeSlot(new TimeSlot(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)))
                .build();
    }

    private MedicalNote existingNote(Long noteId, Appointment appointment) {
        return MedicalNote.builder()
                .noteId(noteId)
                .appointment(appointment)
                .doctorId("doctor-1")
                .patientId("patient-1")
                .noteText("Initial note")
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastUpdatedAt(LocalDateTime.now().minusHours(1))
                .noteType(NoteType.CONSULTATION)
                .build();
    }

    private MedicalNoteRequestDTO noteRequest(Long appointmentId) {
        return MedicalNoteRequestDTO.builder()
                .appointmentId(appointmentId)
                .doctorId("doctor-1")
                .patientId("patient-1")
                .noteText("Updated note")
                .noteType(NoteType.CONSULTATION)
                .build();
    }
}
