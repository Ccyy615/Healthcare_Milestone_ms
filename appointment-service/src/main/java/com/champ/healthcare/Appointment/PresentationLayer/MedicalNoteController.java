package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.BusinessLogicLayer.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical_notes")
@RequiredArgsConstructor
public class MedicalNoteController {

    private final MedicalNoteService medicalNoteService;

    @GetMapping
    public ResponseEntity<List<MedicalNoteResponseDTO>> getAllNotes() {
        return ResponseEntity.ok(medicalNoteService.getAllNotes());
    }

    @GetMapping("/{note_id}")
    public ResponseEntity<MedicalNoteResponseDTO> getNoteById(@PathVariable Long noteId) {
        return ResponseEntity.ok(medicalNoteService.getNoteById(noteId));
    }

    @PostMapping
    public ResponseEntity<MedicalNoteResponseDTO> createNote(@RequestBody MedicalNoteRequestDTO dto) {
        return ResponseEntity.ok(medicalNoteService.createNote(dto));
    }

    @PutMapping("/{note_id}")
    public ResponseEntity<MedicalNoteResponseDTO> updateNote(@PathVariable Long noteId, @RequestBody MedicalNoteRequestDTO dto) {
        return ResponseEntity.ok(medicalNoteService.updateNote(noteId, dto));
    }

    @DeleteMapping("/{note_id}")
    public ResponseEntity<MedicalNoteResponseDTO> deleteNote(@PathVariable Long noteId) {
        return ResponseEntity.ok(medicalNoteService.deleteNote(noteId));
    }
}
