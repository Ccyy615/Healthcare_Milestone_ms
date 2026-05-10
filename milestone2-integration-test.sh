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
RUN_SUFFIX="${RUN_SUFFIX:-$(date +%s)}"
PATIENT_NUMERIC_ID=""
PATIENT_IDENTIFIER=""
DOCTOR_ID=""
ROOM_NUMERIC_ID=""
ROOM_IDENTIFIER=""
APPOINTMENT_ID=""
SECOND_APPOINTMENT_ID=""

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "FAIL: Required command '$command_name' is not installed or not on PATH."
    exit 1
  fi
}

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

assertEqual() {
  assert_equal "$@"
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

assertCurl() {
  assert_status "$@"
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
  local jq_expression="$1"
  printf '%s' "$RESPONSE_BODY" | jq -r "$jq_expression"
}

extract_string() {
  local jq_expression="$1"
  printf '%s' "$RESPONSE_BODY" | jq -r "$jq_expression"
}

cleanup() {
  echo "Cleaning up demo data..."

  if [[ -n "$SECOND_APPOINTMENT_ID" ]]; then
    curl -sS -X DELETE "$BASE_URL/api/v1/appointments/$SECOND_APPOINTMENT_ID" >/dev/null || true
  fi
  if [[ -n "$APPOINTMENT_ID" ]]; then
    curl -sS -X DELETE "$BASE_URL/api/v1/appointments/$APPOINTMENT_ID" >/dev/null || true
  fi
  if [[ -n "$ROOM_NUMERIC_ID" ]]; then
    curl -sS -X DELETE "$BASE_URL/api/v1/clinic-rooms/$ROOM_NUMERIC_ID" >/dev/null || true
  fi
  if [[ -n "$DOCTOR_ID" ]]; then
    curl -sS -X DELETE "$BASE_URL/api/v1/doctors/$DOCTOR_ID" >/dev/null || true
  fi
  if [[ -n "$PATIENT_NUMERIC_ID" ]]; then
    curl -sS -X DELETE "$BASE_URL/api/v1/patients/$PATIENT_NUMERIC_ID" >/dev/null || true
  fi
}

trap cleanup EXIT

require_command curl
require_command jq

echo "Base URL: $BASE_URL"
echo "Running Milestone 2 integration checks through the API gateway only."
echo "Run suffix: $RUN_SUFFIX"

if [[ " $* " == *" start "* ]]; then
  echo "Restarting Docker Compose stack..."
  docker compose down
  docker compose up -d --build
fi

wait_for_service "$BASE_URL/actuator/health"

request POST "/api/v1/patients" '{
  "fullName": "Jordan Miles '"$RUN_SUFFIX"'",
  "dateOfBirth": "1990-05-01",
  "gender": "F",
  "email": "jordan.'"$RUN_SUFFIX"'@example.com",
  "phone": "514-555-0100",
  "street": "1 Main",
  "city": "Montreal",
  "province": "QC",
  "postal_code": "H1H1H1",
  "country": "Canada",
  "insuranceNumber": "INS-'"$RUN_SUFFIX"'",
  "substance": "Pollen",
  "reaction": "Sneezing",
  "bloodType": "O+",
  "status": { "status": "ACTIVE" }
}'
assertCurl 201 "Create patient"
PATIENT_NUMERIC_ID="$(extract_number '.id')"
PATIENT_IDENTIFIER="$(extract_string '.patientId')"
assertEqual "Jordan Miles $RUN_SUFFIX" "$(extract_string '.fullName')" "Patient create response full name"
assert_contains "\"fullName\":\"Jordan Miles $RUN_SUFFIX\"" "Patient create response contains patient data"

request GET "/api/v1/patients/$PATIENT_NUMERIC_ID"
assertCurl 200 "Get patient by id"
assert_contains "\"patientId\":\"$PATIENT_IDENTIFIER\"" "Patient lookup returns created patient"

request GET "/api/v1/patients/patient-identifier/$PATIENT_IDENTIFIER"
assertCurl 200 "Get patient by patient identifier"
assert_contains "\"id\":$PATIENT_NUMERIC_ID" "Patient identifier lookup returns created patient"

request POST "/api/v1/doctors" '{
  "doctorFirstName": "Avery",
  "doctorLastName": "Stone '"$RUN_SUFFIX"'",
  "city": "Montreal",
  "province": "QC",
  "speciality": {
    "speciality": "Cardiology",
    "proficiencyLevel": "Advanced"
  }
}'
assertCurl 201 "Create doctor"
DOCTOR_ID="$(extract_string '.doctorId')"
assertEqual "Avery" "$(extract_string '.doctorFirstName')" "Doctor create response first name"
assert_contains "\"doctorFirstName\":\"Avery\"" "Doctor create response contains doctor data"

request GET "/api/v1/doctors/$DOCTOR_ID"
assertCurl 200 "Get doctor by id"
assert_contains "\"doctorId\":\"$DOCTOR_ID\"" "Doctor lookup returns created doctor"

request POST "/api/v1/doctors/$DOCTOR_ID/license" '{
  "licenseName": "General Practice License",
  "status": "VALID"
}'
assertCurl 200 "Add doctor license"
assert_contains "\"isValid\":true" "Doctor license flow marks doctor as valid"

request POST "/api/v1/doctors/$DOCTOR_ID/activate"
assertCurl 200 "Activate doctor"
assert_contains "\"isActive\":true" "Doctor activation marks doctor as active"

request POST "/api/v1/clinic-rooms" '{
  "roomName": "Consultation Room '"$RUN_SUFFIX"'",
  "roomNumber": "10'"$RUN_SUFFIX"'",
  "roomStatus": {
    "roomStatus": "AVAILABLE"
  }
}'
assertCurl 201 "Create clinic room"
ROOM_NUMERIC_ID="$(extract_number '.id')"
ROOM_IDENTIFIER="$(extract_string '.roomId')"
assertEqual "Consultation Room $RUN_SUFFIX" "$(extract_string '.roomName')" "Clinic room create response name"
assert_contains "\"roomName\":\"Consultation Room $RUN_SUFFIX\"" "Clinic room create response contains room data"

request GET "/api/v1/clinic-rooms/$ROOM_NUMERIC_ID"
assertCurl 200 "Get clinic room by id"
assert_contains "\"roomId\":\"$ROOM_IDENTIFIER\"" "Clinic room lookup returns created room"

request GET "/api/v1/clinic-rooms/room-identifier/$ROOM_IDENTIFIER"
assertCurl 200 "Get clinic room by room identifier"
assert_contains "\"id\":$ROOM_NUMERIC_ID" "Clinic room identifier lookup returns created room"

request POST "/api/v1/appointments" "{
  \"patientId\": \"$PATIENT_IDENTIFIER\",
  \"doctorId\": \"$DOCTOR_ID\",
  \"roomId\": \"$ROOM_IDENTIFIER\",
  \"startTime\": \"2026-05-20T09:00:00\",
  \"endTime\": \"2026-05-20T10:00:00\",
  \"description\": \"Annual checkup\"
}"
assertCurl 201 "Create appointment"
APPOINTMENT_ID="$(extract_number '.appointmentId')"
assertEqual "$PATIENT_IDENTIFIER" "$(extract_string '.patientId')" "Appointment create response patient id"
assert_contains "\"patientId\":\"$PATIENT_IDENTIFIER\"" "Appointment create response contains patient identifier"

request GET "/api/v1/appointments"
assertCurl 200 "Get all appointments"
assert_contains "\"appointmentId\":$APPOINTMENT_ID" "Appointment list includes created appointment"

request GET "/api/v1/appointments/$APPOINTMENT_ID"
assertCurl 200 "Get appointment by id"
assert_contains "\"appointmentId\":$APPOINTMENT_ID" "Appointment lookup returns created appointment"

request GET "/api/v1/appointments/doctor/$DOCTOR_ID"
assertCurl 200 "Get appointments by doctor id"
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
assertCurl 200 "Update appointment"
assert_contains "\"description\":\"Updated annual checkup\"" "Updated appointment response contains new description"

request PATCH "/api/v1/appointments/$APPOINTMENT_ID/complete"
assertCurl 200 "Complete appointment"
assert_contains "\"status\":\"COMPLETED\"" "Completed appointment response contains completed status"

request POST "/api/v1/appointments" "{
  \"patientId\": \"$PATIENT_IDENTIFIER\",
  \"doctorId\": \"$DOCTOR_ID\",
  \"roomId\": \"$ROOM_IDENTIFIER\",
  \"startTime\": \"2026-05-20T11:00:00\",
  \"endTime\": \"2026-05-20T12:00:00\",
  \"description\": \"Follow-up appointment\"
}"
assertCurl 201 "Create second appointment for cancel/delete flow"
SECOND_APPOINTMENT_ID="$(extract_number '.appointmentId')"

request PATCH "/api/v1/appointments/$SECOND_APPOINTMENT_ID/cancel"
assertCurl 200 "Cancel appointment"
assert_contains "\"status\":\"CANCELLED\"" "Cancelled appointment response contains cancelled status"

request DELETE "/api/v1/appointments/$SECOND_APPOINTMENT_ID"
assertCurl 200 "Delete appointment"
assert_contains "\"appointmentId\":$SECOND_APPOINTMENT_ID" "Delete appointment response contains deleted identifier"
SECOND_APPOINTMENT_ID=""

request DELETE "/api/v1/appointments/$APPOINTMENT_ID"
assertCurl 200 "Delete primary appointment"
assert_contains "\"appointmentId\":$APPOINTMENT_ID" "Delete primary appointment response contains deleted identifier"
APPOINTMENT_ID=""

echo "All Milestone 2 gateway integration checks passed."

if [[ " $* " == *" stop "* ]]; then
  echo "Stopping Docker Compose stack..."
  docker compose down
fi
