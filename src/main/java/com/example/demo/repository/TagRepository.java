package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByCategoryIgnoreCase(String category);

    List<Tag> findByColorCode(String colorCode);

    boolean existsByNameIgnoreCase(String name);
}
