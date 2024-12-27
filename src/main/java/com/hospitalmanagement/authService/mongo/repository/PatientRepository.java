package com.hospitalmanagement.authService.mongo.repository;

import com.hospitalmanagement.authService.mongo.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface PatientRepository extends MongoRepository<Patient, String> {
}
