package com.example.demo.repository;

import java.time.LocalDate;

/**
 * DTO hakuehtojen keräämiseen Criteria API -kyselyä varten.
 * Kaikki kentät ovat valinnaisia – predikaatti lisätään vain, jos arvo on
 * annettu.
 */
public class CatSearchFilter {

    /** Osittainen tekstihaku kissan nimellä (LIKE) */
    private String name;

    /** Osittainen tekstihaku rodulla (LIKE) */
    private String breed;

    /** Osittainen tekstihaku värillä (LIKE) */
    private String color;

    /** Sukupuoli – tarkka täsmäys */
    private String gender;

    /** Saapumispäivä aikavälillä */
    private LocalDate arrivalFrom;
    private LocalDate arrivalTo;

    /** JOIN Tag – tagin nimen osittainen haku */
    private String tagName;

    /** JOIN Tag – tagin kategoria (tarkka täsmäys) */
    private String tagCategory;

    /**
     * Monimutkainen OR-ehto:
     * (nimi SISÄLTÄÄ nameOrBreed TAI rotu SISÄLTÄÄ nameOrBreed) AND color/gender
     * jne.
     */
    private String nameOrBreed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getArrivalFrom() {
        return arrivalFrom;
    }

    public void setArrivalFrom(LocalDate arrivalFrom) {
        this.arrivalFrom = arrivalFrom;
    }

    public LocalDate getArrivalTo() {
        return arrivalTo;
    }

    public void setArrivalTo(LocalDate arrivalTo) {
        this.arrivalTo = arrivalTo;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagCategory() {
        return tagCategory;
    }

    public void setTagCategory(String tagCategory) {
        this.tagCategory = tagCategory;
    }

    public String getNameOrBreed() {
        return nameOrBreed;
    }

    public void setNameOrBreed(String nameOrBreed) {
        this.nameOrBreed = nameOrBreed;
    }
}
