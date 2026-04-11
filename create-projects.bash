#!/usr/bin/env bash

spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=service-requester-service \
--package-name=com.champ.healthcare.Appointment \
--groupId=com.champ.healthcare.Appointment \
--dependencies=web \
--version=1.0.0-SNAPSHOT \
appointment-service

spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=handyman-profile-service \
--package-name=com.champ.healthcare.ClinicRoom \
--groupId=com.champ.healthcare.ClinicRoom \
--dependencies=web \
--version=1.0.0-SNAPSHOT \
clinic-room-service

spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=payment-context-service \
--package-name=com.champ.healthcare.Doctor \
--groupId=com.champ.healthcare.Doctor \
--dependencies=web \
--version=1.0.0-SNAPSHOT \
doctor-service

spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=review_context-service \
--package-name=com.champ.healthcare.Patient \
--groupId=com.champ.healthcare.Patient \
--dependencies=web \
--version=1.0.0-SNAPSHOT \
patient-service

spring init \
--boot-version=3.5.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=apigateway-service \
--package-name=com.champ.healthcare.ApiGateway \
--groupId=com.champ.healthcare.ApiGateway \
--dependencies=web \
--version=1.0.0-SNAPSHOT \
apigateway-service

