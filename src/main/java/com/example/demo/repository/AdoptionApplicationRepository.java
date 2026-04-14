package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AdoptionApplication;
import com.example.demo.entity.AdoptionStatus;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {

    List<AdoptionApplication> findByCat_Id(Long catId);

    List<AdoptionApplication> findByStatus(AdoptionStatus status);

    List<AdoptionApplication> findByApplicationDateAfter(LocalDate date);

    List<AdoptionApplication> findByApplicantEmailContainingIgnoreCase(String emailPart);
}
