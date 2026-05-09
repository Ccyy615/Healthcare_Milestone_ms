#!/usr/bin/env bash

set -euo pipefail

# Sample usage:
#   ./milestone2-integration-test.sh
#   ./milestone2-integration-test.sh start
#   ./milestone2-integration-test.sh start stop
#   HOST=localhost PORT=8080 ./milestone2-integration-test.sh
#
# All requests go through the API Gateway only.

: "${HOST:=localhost}"
: "${PORT:=8080}"
BASE_URL="${BASE_URL:-http://${HOST}:${PORT}}"
RESPONSE_BODY=""
RESPONSE_STATUS=""

assert_equal() {
  local expected="$1"
  local actual="$2"
  local label="$3"

  if [[ "$expected" != "$actual" ]]; then
    echo "FAIL: $label"
    echo "Expected: $expected"
    echo "Actual:   $actual"
    exit 1
  fi

  echo "PASS: $label"
}

request() {
  local method="$1"
  local path="$2"
  local payload="${3-}"
  local response_file

  response_file="$(mktemp)"

  if [[ -n "$payload" ]]; then
    RESPONSE_STATUS="$(curl -sS -X "$method" \
      -H "Content-Type: application/json" \
      -o "$response_file" \
      -w "%{http_code}" \
      -d "$payload" \
      "$BASE_URL$path")"
  else
    RESPONSE_STATUS="$(curl -sS -X "$method" \
      -o "$response_file" \
      -w "%{http_code}" \
      "$BASE_URL$path")"
  fi

  RESPONSE_BODY="$(tr -d '\r\n' < "$response_file")"
  rm -f "$response_file"
}

assert_status() {
  local expected="$1"
  local label="$2"

  if [[ "$RESPONSE_STATUS" != "$expected" ]]; then
    echo "FAIL: $label"
    echo "Expected HTTP $expected but received $RESPONSE_STATUS"
    echo "Response: $RESPONSE_BODY"
    exit 1
  fi

  echo "PASS: $label ($RESPONSE_STATUS)"
}

assert_contains() {
  local needle="$1"
  local label="$2"

  if [[ "$RESPONSE_BODY" != *"$needle"* ]]; then
    echo "FAIL: $label"
    echo "Expected response to contain: $needle"
    echo "Response: $RESPONSE_BODY"
    exit 1
  fi

  echo "PASS: $label"
}

test_url() {
  local url="$1"
  curl "$url" -ks -f -o /dev/null
}

wait_for_service() {
  local url="$1"
  local attempt=0

  echo -n "Waiting for $url"
  until test_url "$url"; do
    attempt=$((attempt + 1))
    if [[ "$attempt" -ge 60 ]]; then
      echo
      echo "FAIL: Service did not become ready: $url"
      exit 1
    fi

    echo -n "."
    sleep 5
  done

  echo
  echo "PASS: Service is reachable"
}

extract_number() {
  local key="$1"
  printf '%s' "$RESPONSE_BODY" | sed -nE "s/.*\"$key\":([0-9]+).*/\\1/p"
}

extract_string() {
  local key="$1"
  printf '%s' "$RESPONSE_BODY" | sed -nE "s/.*\"$key\":\"([^\"]+)\".*/\\1/p"
}

echo "Base URL: $BASE_URL"
echo "Running Milestone 2 integration checks through the API gateway only."

if [[ " $* " == *" start "* ]]; then
  echo "Restarting Docker Compose stack..."
  docker compose down
  docker compose up -d --build
fi

wait_for_service "$BASE_URL/actuator/health"

request POST "/api/v1/patients" '{
  "fullName": "Jordan Miles",
  "dateOfBirth": "1990-05-01",
  "gender": "F",
  "email": "jordan@example.com",
  "phone": "514-555-0100",
  "street": "1 Main",
  "city": "Montreal",
  "province": "QC",
  "postal_code": "H1H1H1",
  "country": "Canada",
  "insuranceNumber": "INS-1",
  "substance": "Pollen",
  "reaction": "Sneezing",
  "bloodType": "O+",
  "status": { "status": "ACTIVE" }
}'
assert_status 201 "Create patient"
PATIENT_NUMERIC_ID="$(extract_number "id")"
PATIENT_IDENTIFIER="$(extract_string "patientId")"
assert_equal "Jordan Miles" "$(extract_string "fullName")" "Patient create response full name"
assert_contains "\"fullName\":\"Jordan Miles\"" "Patient create response contains patient data"

request GET "/api/v1/patients/$PATIENT_NUMERIC_ID"
assert_status 200 "Get patient by id"
assert_contains "\"patientId\":\"$PATIENT_IDENTIFIER\"" "Patient lookup returns created patient"

request POST "/api/v1/doctors" '{
  "doctorFirstName": "Avery",
  "doctorLastName": "Stone",
  "city": "Montreal",
  "province": "QC",
  "speciality": {
    "speciality": "Cardiology",
    "proficiencyLevel": "Advanced"
  }
}'
assert_status 201 "Create doctor"
DOCTOR_ID="$(extract_string "doctorId")"
assert_equal "Avery" "$(extract_string "doctorFirstName")" "Doctor create response first name"
assert_contains "\"doctorFirstName\":\"Avery\"" "Doctor create response contains doctor data"

request GET "/api/v1/doctors/$DOCTOR_ID"
assert_status 200 "Get doctor by id"
assert_contains "\"doctorId\":\"$DOCTOR_ID\"" "Doctor lookup returns created doctor"

request POST "/api/v1/doctors/$DOCTOR_ID/license" '{
  "licenseName": "General Practice License",
  "status": "VALID"
}'
assert_status 200 "Add doctor license"
assert_contains "\"isValid\":true" "Doctor license flow marks doctor as valid"

request POST "/api/v1/doctors/$DOCTOR_ID/activate"
assert_status 200 "Activate doctor"
assert_contains "\"isActive\":true" "Doctor activation marks doctor as active"

request POST "/api/v1/clinic-rooms" '{
  "roomName": "Consultation Room A",
  "roomNumber": "101",
  "roomStatus": {
    "roomStatus": "AVAILABLE"
  }
}'
assert_status 201 "Create clinic room"
ROOM_NUMERIC_ID="$(extract_number "id")"
ROOM_IDENTIFIER="$(extract_string "roomId")"
assert_equal "Consultation Room A" "$(extract_string "roomName")" "Clinic room create response name"
assert_contains "\"roomName\":\"Consultation Room A\"" "Clinic room create response contains room data"

request GET "/api/v1/clinic-rooms/$ROOM_NUMERIC_ID"
assert_status 200 "Get clinic room by id"
assert_contains "\"roomId\":\"$ROOM_IDENTIFIER\"" "Clinic room lookup returns created room"

request POST "/api/v1/appointments" "{
  \"patientId\": \"$PATIENT_IDENTIFIER\",
  \"doctorId\": \"$DOCTOR_ID\",
  \"roomId\": \"$ROOM_IDENTIFIER\",
  \"startTime\": \"2026-05-20T09:00:00\",
  \"endTime\": \"2026-05-20T10:00:00\",
  \"description\": \"Annual checkup\"
}"
assert_status 201 "Create appointment"
APPOINTMENT_ID="$(extract_number "appointmentId")"
assert_equal "$PATIENT_IDENTIFIER" "$(extract_string "patientId")" "Appointment create response patient id"
assert_contains "\"patientId\":\"$PATIENT_IDENTIFIER\"" "Appointment create response contains patient identifier"

request GET "/api/v1/appointments"
assert_status 200 "Get all appointments"
assert_contains "\"appointmentId\":$APPOINTMENT_ID" "Appointment list includes created appointment"

request GET "/api/v1/appointments/$APPOINTMENT_ID"
assert_status 200 "Get appointment by id"
assert_contains "\"appointmentId\":$APPOINTMENT_ID" "Appointment lookup returns created appointment"

request GET "/api/v1/appointments/doctor/$DOCTOR_ID"
assert_status 200 "Get appointments by doctor id"
assert_contains "\"doctorId\":\"$DOCTOR_ID\"" "Doctor appointment list includes created appointment"

request PUT "/api/v1/appointments/$APPOINTMENT_ID" "{
  \"patientId\": \"$PATIENT_IDENTIFIER\",
  \"doctorId\": \"$DOCTOR_ID\",
  \"roomId\": \"$ROOM_IDENTIFIER\",
  \"startTime\": \"2026-05-20T09:30:00\",
  \"endTime\": \"2026-05-20T10:30:00\",
  \"description\": \"Updated annual checkup\",
  \"status\": \"CONFIRMED\"
}"
assert_status 200 "Update appointment"
assert_contains "\"description\":\"Updated annual checkup\"" "Updated appointment response contains new description"

request PATCH "/api/v1/appointments/$APPOINTMENT_ID/complete"
assert_status 200 "Complete appointment"
assert_contains "\"status\":\"COMPLETED\"" "Completed appointment response contains completed status"

request POST "/api/v1/appointments" "{
  \"patientId\": \"$PATIENT_IDENTIFIER\",
  \"doctorId\": \"$DOCTOR_ID\",
  \"roomId\": \"$ROOM_IDENTIFIER\",
  \"startTime\": \"2026-05-20T11:00:00\",
  \"endTime\": \"2026-05-20T12:00:00\",
  \"description\": \"Follow-up appointment\"
}"
assert_status 201 "Create second appointment for cancel/delete flow"
SECOND_APPOINTMENT_ID="$(extract_number "appointmentId")"

request PATCH "/api/v1/appointments/$SECOND_APPOINTMENT_ID/cancel"
assert_status 200 "Cancel appointment"
assert_contains "\"status\":\"CANCELLED\"" "Cancelled appointment response contains cancelled status"

request DELETE "/api/v1/appointments/$SECOND_APPOINTMENT_ID"
assert_status 200 "Delete appointment"
assert_contains "\"appointmentId\":$SECOND_APPOINTMENT_ID" "Delete appointment response contains deleted identifier"

echo "All Milestone 2 gateway integration checks passed."

if [[ " $* " == *" stop "* ]]; then
  echo "Stopping Docker Compose stack..."
  docker compose down
fi
