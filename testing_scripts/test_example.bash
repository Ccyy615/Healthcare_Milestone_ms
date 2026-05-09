....................................323666











#!/usr/bin/env bash

set -euo pipefail

: "${HOST:=localhost}"
: "${PORT:=8080}"

echo "Fetching patients from http://${HOST}:${PORT}/api/v1/patients"
response="$(curl -s "http://${HOST}:${PORT}/api/v1/patients")"

echo "Raw Response: $response"

firstPatientId="$(echo "$response" | jq -r '.[0].patientId')"
firstPatientName="$(echo "$response" | jq -r '.[0].fullName')"

echo "First Patient ID: $firstPatientId"
echo "First Patient Name: $firstPatientName"
