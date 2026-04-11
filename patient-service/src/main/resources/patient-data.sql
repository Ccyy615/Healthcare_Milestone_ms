-- //////////////////////////////////////////
-- Data for patients
-- //////////////////////////////////////////

INSERT INTO patients(
    id, patient_id, full_name, date_of_birth, gender,
    contact_email, contact_phone,
    street, city, province, postal_code, country,
    insurancenumber, allergy_substance, allergy_reaction,
    blood_type, patient_status
) VALUES
      (1, 'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6', 'John Smith', '1985-03-15', 'M',
       'john.smith@example.com', '514-555-0101',
       '123 Maple St', 'Montreal', 'Quebec', 'H1A1A1', 'Canada',
       'INS-1001', 'Peanuts', 'Anaphylaxis',
       'AB', 'ACTIVE'),

      (2, 'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b9a2', 'Emily Johnson', '1990-07-22', 'F',
       'emily.johnson@example.com', '514-555-0102',
       '456 Oak St', 'Laval', 'Quebec', 'H7B2B2', 'Canada',
       'INS-1002', 'None', 'None',
       'A', 'ACTIVE'),

      (3, 'e3f4a5b6-c7d8-e9f0-a1b2-c3d4e5f6a7b8', 'Michael Brown', '1978-11-05', 'M',
       'michael.brown@example.com', '514-555-0103',
       '789 Pine St', 'Toronto', 'Ontario', 'M5C1C1', 'Canada',
       'INS-1003', 'Gluten', 'Anaphylaxis',
       'B', 'ACTIVE'),

      (4, 'f4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9', 'Sarah Davis', '1995-01-18', 'F',
       'sarah.davis@example.com', '514-555-0104',
       '321 Birch St', 'Montreal', 'Quebec', 'H2D3D3', 'Canada',
       'INS-1004', 'Penicillin', 'Anaphylaxis',
       'O', 'ACTIVE'),

      (5, 'a5b6c7d8-e9f0-a1b2-c3d4-e5f6a7b8c9d0', 'David Lee', '1982-05-30', 'M',
       'david.lee@example.com', '514-555-0105',
       '654 Cedar St', 'Toronto', 'Ontario', 'M4E2E2', 'Canada',
       'INS-1005', 'None', 'None',
       'A', 'ACTIVE');
