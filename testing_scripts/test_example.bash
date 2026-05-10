#!/usr/bin/env bash

set -euo pipefail

: "${HOST:=localhost}"
: "${PORT:=8080}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required for this example script."
  exit 1
fi

echo "Fetching patients from http://${HOST}:${PORT}/api/v1/patients"
response="$(curl -sS "http://${HOST}:${PORT}/api/v1/patients")"

echo "Raw Response: $response"

first_patient_id="$(echo "$response" | jq -r '.[0].patientId')"
first_patient_name="$(echo "$response" | jq -r '.[0].fullName')"

echo "First Patient ID: $first_patient_id"
echo "First Patient Name: $first_patient_name"
