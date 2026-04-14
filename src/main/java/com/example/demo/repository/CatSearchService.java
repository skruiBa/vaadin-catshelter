package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Cat;
import com.example.demo.entity.Tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Criteria API -hakupalvelu kissojen suodattamiseen.
 *
 * Tuetuut predikaatit:
 * 1. LIKE-osumhaku kenttiin name, breed, color
 * 2. Aikavälisuodatus arrivalDate BETWEEN arrivalFrom AND arrivalTo
 * 3. JOIN Tag-tauluun ja tagin nimen LIKE-suodatus
 * 4. JOIN Tag-tauluun ja tagin kategorian tarkka täsmäys
 * 5. Monimutkainen (name LIKE x OR breed LIKE x) AND muut ehdot
 */
@Service
public class CatSearchService {

    @PersistenceContext
    private EntityManager em;

    public List<Cat> search(CatSearchFilter f) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cat> cq = cb.createQuery(Cat.class);
        Root<Cat> cat = cq.from(Cat.class);

        // JOIN tags-relaatioon (LEFT JOIN, jotta kissat ilman tageja myös palaavat)
        Join<Cat, Tag> tagJoin = cat.join("tags", JoinType.LEFT);
        cq.distinct(true);

        List<Predicate> predicates = new ArrayList<>();

        // 1. Yksittäiset LIKE-haut (name, breed, color)
        if (hasValue(f.getName())) {
            predicates.add(cb.like(cb.lower(cat.get("name")),
                    "%" + f.getName().toLowerCase() + "%"));
        }
        if (hasValue(f.getBreed())) {
            predicates.add(cb.like(cb.lower(cat.get("breed")),
                    "%" + f.getBreed().toLowerCase() + "%"));
        }
        if (hasValue(f.getColor())) {
            predicates.add(cb.like(cb.lower(cat.get("color")),
                    "%" + f.getColor().toLowerCase() + "%"));
        }

        // 2. Sukupuoli – tarkka täsmäys enumin nimellä
        if (hasValue(f.getGender())) {
            predicates.add(cb.equal(
                    cb.lower(cat.get("gender").as(String.class)),
                    f.getGender().toLowerCase()));
        }

        // 3. Aikavälisuodatus (BETWEEN)
        if (f.getArrivalFrom() != null && f.getArrivalTo() != null) {
            predicates.add(cb.between(cat.get("arrivalDate"),
                    f.getArrivalFrom(), f.getArrivalTo()));
        } else if (f.getArrivalFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(cat.get("arrivalDate"), f.getArrivalFrom()));
        } else if (f.getArrivalTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(cat.get("arrivalDate"), f.getArrivalTo()));
        }

        // 4. JOIN Tag – tagin nimen LIKE-suodatus
        if (hasValue(f.getTagName())) {
            predicates.add(cb.like(cb.lower(tagJoin.get("name")),
                    "%" + f.getTagName().toLowerCase() + "%"));
        }

        // 5. JOIN Tag – tagin kategorian tarkka täsmäys
        if (hasValue(f.getTagCategory())) {
            predicates.add(cb.equal(cb.lower(tagJoin.get("category")),
                    f.getTagCategory().toLowerCase()));
        }

        // 6. Monimutkainen OR-AND-ehto: (name LIKE x OR breed LIKE x)
        if (hasValue(f.getNameOrBreed())) {
            String pattern = "%" + f.getNameOrBreed().toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(cat.get("name")), pattern);
            Predicate breedLike = cb.like(cb.lower(cat.get("breed")), pattern);
            predicates.add(cb.or(nameLike, breedLike));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.asc(cat.get("name")));

        return em.createQuery(cq).getResultList();
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
