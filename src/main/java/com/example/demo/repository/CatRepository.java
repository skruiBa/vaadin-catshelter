package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Cat;
import com.example.demo.entity.Gender;

public interface CatRepository extends JpaRepository<Cat, Long> {

    List<Cat> findByNameContainingIgnoreCase(String name);

    List<Cat> findByBreedIgnoreCase(String breed);

    List<Cat> findByArrivalDateBetween(LocalDate startDate, LocalDate endDate);

    List<Cat> findByTags_NameIgnoreCase(String tagName);

    long countByGender(Gender gender);
}
