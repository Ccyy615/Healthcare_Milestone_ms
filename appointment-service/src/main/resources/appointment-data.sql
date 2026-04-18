-- //////////////////////////////////////////
-- Data for booking appointment
-- Double-booking is checked in Appointment on room_id + overlapping time
-- //////////////////////////////////////////

INSERT INTO appointments(
    appointment_id,
    patient_id,
    doctor_id,
    room_id,
    appointment_status,
    created_at,
    appointment_start,
    appointment_end,
    descriptions
) VALUES
      (1,
       'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6',
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'r1a2b3c4-d5e6-47f8-9a10-111111111111',
       'CONFIRMED',
       '2026-03-16 10:00:00',
       '2026-03-18 09:00:00',
       '2026-03-18 09:30:00',
       'General check-up'),

      (2,
       'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b9a2',
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'r2a2b3c4-d5e6-47f8-9a10-222222222222',
       'CONFIRMED',
       '2026-03-16 10:05:00',
       '2026-03-18 10:00:00',
       '2026-03-18 10:30:00',
       'Follow-up consultation'),

      (3,
       'e3f4a5b6-c7d8-e9f0-a1b2-c3d4e5f6a7b8',
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'r1a2b3c4-d5e6-47f8-9a10-111111111111',
       'COMPLETED',
       '2026-03-16 10:10:00',
       '2026-03-17 14:00:00',
       '2026-03-17 14:30:00',
       'Routine blood pressure check'),

      (4,
       'f4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9',
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'r2a2b3c4-d5e6-47f8-9a10-222222222222',
       'CANCELLED',
       '2026-03-16 10:15:00',
       '2026-03-19 11:00:00',
       '2026-03-19 11:30:00',
       'Patient cancelled due to illness'),

      (5,
       'a5b6c7d8-e9f0-a1b2-c3d4-e5f6a7b8c9d0',
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'r1a2b3c4-d5e6-47f8-9a10-111111111111',
       'CONFIRMED',
       '2026-03-16 10:20:00',
       '2026-03-20 15:00:00',
       '2026-03-20 15:30:00',
       'Skin irritation consultation');


-- //////////////////////////////////////////
-- Data for medical notes
-- Keep this only if MedicalNote mapping starts cleanly
-- //////////////////////////////////////////

INSERT INTO medical_notes(
    note_id, appointment_id, doctor_id, patient_id, text,
    note_create_at, last_update, note_type
) VALUES
      (1, 1,
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6',
       'Patient reported mild headaches for the past three days. Blood pressure slightly elevated.',
       '2026-03-18 09:35:00',
       '2026-03-18 09:35:00',
       'CONSULTATION'),

      (2, 2,
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b9a2',
       'Follow-up visit after flu treatment. Symptoms improving, no fever detected.',
       '2026-03-18 10:35:00',
       '2026-03-18 10:35:00',
       'FOLLOW_UP'),

      (3, 3,
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'e3f4a5b6-c7d8-e9f0-a1b2-c3d4e5f6a7b8',
       'Routine blood pressure check. Patient advised to reduce salt intake.',
       '2026-03-17 14:35:00',
       '2026-03-17 14:35:00',
       'PRESCRIPTION'),

      (4, 4,
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'f4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9',
       'Appointment cancelled by patient due to illness.',
       '2026-03-19 11:35:00',
       '2026-03-19 11:35:00',
       'FOLLOW_UP'),

      (5, 5,
       'e1f2a3b4-c5d6-47e8-9f01-23456789abcd',
       'a5b6c7d8-e9f0-a1b2-c3d4-e5f6a7b8c9d0',
       'Patient presented with mild skin irritation on forearm. Prescribed topical cream.',
       '2026-03-20 15:35:00',
       '2026-03-20 15:35:00',
       'CONSULTATION');