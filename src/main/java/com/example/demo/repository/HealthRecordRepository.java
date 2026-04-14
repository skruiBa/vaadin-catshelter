package com.example.demo.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.HealthRecord;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    Optional<HealthRecord> findByCat_Id(Long catId);

    boolean existsByCat_Id(Long catId);

    long countByLastCheckupBefore(LocalDate date);
}
