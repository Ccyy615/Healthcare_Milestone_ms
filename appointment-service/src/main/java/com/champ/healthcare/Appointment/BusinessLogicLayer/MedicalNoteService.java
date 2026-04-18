package com.champ.healthcare.Appointment.BusinessLogicLayer;

import com.champ.healthcare.Appointment.DataAccessLayer.AppointmentRepository;
import com.champ.healthcare.Appointment.DataAccessLayer.MedicalNoteRepository;
import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.MedicalNote;
import com.champ.healthcare.Appointment.Mapper.MedicalNoteMapper;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteResponseDTO;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalNoteService {

    private final MedicalNoteRepository medicalNoteRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<MedicalNoteResponseDTO> getAllNotes() {
        return MedicalNoteMapper.toResponseDTOList(medicalNoteRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MedicalNoteResponseDTO getNoteById(Long noteId) {
        MedicalNote note = medicalNoteRepository
                .findByNoteId(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note not found with ID: " + noteId));

        return MedicalNoteMapper.toResponseDTO(note);
    }

    @Transactional
    public MedicalNoteResponseDTO createNote(MedicalNoteRequestDTO dto) {
        Appointment appointment = appointmentRepository
                .findByAppointmentId(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + dto.getAppointmentId()));

        MedicalNote note = MedicalNoteMapper.toEntity(dto, appointment);

        if (note.getCreatedAt() == null) {
            note.setCreatedAt(LocalDateTime.now());
        }

        note.setLastUpdatedAt(LocalDateTime.now());

        MedicalNote savedNote = medicalNoteRepository.save(note);
        return MedicalNoteMapper.toResponseDTO(savedNote);
    }

    @Transactional
    public MedicalNoteResponseDTO updateNote(Long noteId, MedicalNoteRequestDTO dto) {
        MedicalNote note = medicalNoteRepository
                .findByNoteId(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note not found with ID: " + noteId));

        if (dto.getAppointmentId() != null &&
                (note.getAppointment() == null ||
                        !dto.getAppointmentId().equals(note.getAppointment().getAppointmentId()))) {

            Appointment appointment = appointmentRepository
                    .findByAppointmentId(dto.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Appointment not found with ID: " + dto.getAppointmentId()));

            note.setAppointment(appointment);
        }

        note.setDoctorId(dto.getDoctorId());
        note.setPatientId(dto.getPatientId());
        note.setNoteText(dto.getNoteText());
        note.setNoteType(dto.getNoteType());
        note.setLastUpdatedAt(LocalDateTime.now());

        MedicalNote savedNote = medicalNoteRepository.save(note);
        return MedicalNoteMapper.toResponseDTO(savedNote);
    }

    @Transactional
    public MedicalNoteResponseDTO deleteNote(Long noteId) {
        MedicalNote note = medicalNoteRepository
                .findByNoteId(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note not found with ID: " + noteId));

        medicalNoteRepository.delete(note);
        return MedicalNoteMapper.toResponseDTO(note);
    }
}