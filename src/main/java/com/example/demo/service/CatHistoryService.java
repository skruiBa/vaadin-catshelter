package com.example.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Cat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CatHistoryService {

    @PersistenceContext
    private EntityManager em;

    public List<CatHistoryRow> findCatHistory(Long catId) {
        if (catId == null) {
            return List.of();
        }

        AuditReader reader = AuditReaderFactory.get(em);
        @SuppressWarnings("unchecked")
        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(Cat.class, false, true)
                .add(AuditEntity.id().eq(catId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        List<CatHistoryRow> rows = new ArrayList<>();
        for (Object[] row : revisions) {
            Cat cat = (Cat) row[0];
            DefaultRevisionEntity revEntity = (DefaultRevisionEntity) row[1];
            RevisionType revisionType = (RevisionType) row[2];

            String tagText = cat != null && cat.getTags() != null
                    ? cat.getTags().stream().map(t -> t.getName()).sorted(Comparator.naturalOrder())
                            .collect(Collectors.joining(", "))
                    : "";

            rows.add(new CatHistoryRow(
                    revEntity.getId(),
                    revisionType == null ? "UNKNOWN" : revisionType.name(),
                    Instant.ofEpochMilli(revEntity.getTimestamp()),
                    cat != null ? cat.getName() : "",
                    cat != null ? cat.getBreed() : "",
                    cat != null ? cat.getColor() : "",
                    cat != null ? cat.getWeight() : null,
                    cat != null ? cat.getArrivalDate() : null,
                    tagText));
        }

        return rows;
    }

    public record CatHistoryRow(
            int revision,
            String action,
            Instant timestamp,
            String name,
            String breed,
            String color,
            Double weight,
            java.time.LocalDate arrivalDate,
            String tags) {
    }
}
