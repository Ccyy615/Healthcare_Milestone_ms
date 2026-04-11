package com.champ.healthcare.Appointment.Mapper;

import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.MedicalNote;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MedicalNoteMapper {

    public static MedicalNoteResponseDTO toResponseDTO(MedicalNote note) {
        if (note == null) return null;

        return MedicalNoteResponseDTO.builder()
                .noteId(note.getNoteId())
                .appointmentId(note.getAppointment())
                .doctorId(note.getDoctorId())
                .patientId(note.getPatientId())
                .noteText(note.getNoteText())
                .createdAt(note.getCreatedAt())
                .lastUpdatedAt(note.getLastUpdatedAt())
                .noteType(note.getNoteType())
                .build();
    }

    public static MedicalNote toEntity(MedicalNoteRequestDTO noteRequestDTO) {
        return MedicalNote.builder()
                .appointment(noteRequestDTO.getAppointmentId())
                .doctorId(noteRequestDTO.getDoctorId())
                .patientId(noteRequestDTO.getPatientId())
                .noteText(noteRequestDTO.getNoteText())
                .createdAt(noteRequestDTO.getCreatedAt())
                .lastUpdatedAt(noteRequestDTO.getLastUpdatedAt())
                .noteType(noteRequestDTO.getNoteType())
                .build();
    }

    public static List<MedicalNoteResponseDTO> toResponseDTOList(List<MedicalNote> notes) {
        return notes.stream().map(MedicalNoteMapper::toResponseDTO).collect(Collectors.toList());
    }

}
