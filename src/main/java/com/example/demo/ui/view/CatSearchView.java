package com.example.demo.ui.view;

import java.util.List;

import com.example.demo.entity.Cat;
import com.example.demo.entity.Gender;
import com.example.demo.repository.CatSearchFilter;
import com.example.demo.repository.CatSearchService;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/**
 * Criteria API -hakunäkymä.
 * Hakee kissat dynaamisilla predikaateilla JPA Criteria API:n kautta.
 */
@Route(value = "haku", layout = MainLayout.class)
@PageTitle("Haku | CatShelter")
@PermitAll
public class CatSearchView extends VerticalLayout {

    private final CatSearchService searchService;

    // --- Hakukentät ---
    private final TextField nameOrBreed = new TextField("Nimi tai rotu (OR-haku)");
    private final TextField name = new TextField("Nimi");
    private final TextField breed = new TextField("Rotu");
    private final TextField color = new TextField("Väri");
    private final ComboBox<Gender> gender = new ComboBox<>("Sukupuoli");
    private final DatePicker arrivalFrom = new DatePicker("Saapunut alkaen");
    private final DatePicker arrivalTo = new DatePicker("Saapunut päätyen");
    private final TextField tagName = new TextField("Tagin nimi");
    private final TextField tagCategory = new TextField("Tagin kategoria");

    private final Grid<Cat> grid = new Grid<>(Cat.class, false);
    private final Span resultCount = new Span();

    public CatSearchView(CatSearchService searchService) {
        this.searchService = searchService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader(), buildFilterPanel(), buildResultBar(), buildGrid());

        // Näytetään kaikki ladattaessa ilman suodatusta
        runSearch();
    }

    // -----------------------------------------------------------------------
    // Layout helpers
    // -----------------------------------------------------------------------

    private H2 buildHeader() {
        H2 title = new H2("Criteria API -haku");
        title.getStyle().set("margin-bottom", "0");
        return title;
    }

    private VerticalLayout buildFilterPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setPadding(true);
        panel.setSpacing(true);
        panel.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        // OR-hakukenttä – koko leveys omalla rivillään
        nameOrBreed.setPlaceholder("esim. Misu  →  nimi TAI rotu sisältää...");
        nameOrBreed.setWidthFull();
        nameOrBreed.setPrefixComponent(VaadinIcon.SEARCH.create());

        // Muut yksittäiset kentät
        FlexLayout fields = new FlexLayout(name, breed, color, gender, tagName, tagCategory);
        fields.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        fields.getStyle().set("gap", "0.75rem");

        // Aikaväli
        HorizontalLayout dateRange = new HorizontalLayout(arrivalFrom, arrivalTo);
        dateRange.setSpacing(true);

        // Napit
        Button search = new Button("Hae", VaadinIcon.SEARCH.create(), e -> runSearch());
        search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button clear = new Button("Tyhjennä", VaadinIcon.CLOSE.create(), e -> clearFilters());
        clear.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(search, clear);
        buttons.setSpacing(true);

        gender.setItems(Gender.values());
        gender.setItemLabelGenerator(g -> switch (g) {
            case MALE -> "Uros";
            case FEMALE -> "Naaras";
            case UNKNOWN -> "Tuntematon";
        });

        panel.add(nameOrBreed, fields, dateRange, buttons);
        return panel;
    }

    private HorizontalLayout buildResultBar() {
        resultCount.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        HorizontalLayout bar = new HorizontalLayout(resultCount);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        return bar;
    }

    private Grid<Cat> buildGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();

        grid.addColumn(Cat::getName).setHeader("Nimi").setSortable(true).setKey("name");
        grid.addColumn(Cat::getBreed).setHeader("Rotu").setSortable(true);
        grid.addColumn(Cat::getColor).setHeader("Väri");
        grid.addColumn(c -> c.getGender() == null ? ""
                : (c.getGender() == Gender.MALE ? "Uros" : c.getGender() == Gender.FEMALE ? "Naaras" : "Tuntematon"))
                .setHeader("Sukupuoli");
        grid.addColumn(Cat::getArrivalDate).setHeader("Saapunut").setSortable(true);
        grid.addColumn(c -> c.getTags() == null ? ""
                : c.getTags().stream()
                        .map(t -> t.getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(""))
                .setHeader("Tagit");

        return grid;
    }

    // -----------------------------------------------------------------------
    // Logic
    // -----------------------------------------------------------------------

    private void runSearch() {
        CatSearchFilter filter = new CatSearchFilter();
        filter.setNameOrBreed(trimOrNull(nameOrBreed.getValue()));
        filter.setName(trimOrNull(name.getValue()));
        filter.setBreed(trimOrNull(breed.getValue()));
        filter.setColor(trimOrNull(color.getValue()));
        filter.setGender(gender.getValue() == null ? null : gender.getValue().name());
        filter.setArrivalFrom(arrivalFrom.getValue());
        filter.setArrivalTo(arrivalTo.getValue());
        filter.setTagName(trimOrNull(tagName.getValue()));
        filter.setTagCategory(trimOrNull(tagCategory.getValue()));

        List<Cat> results = searchService.search(filter);
        grid.setItems(results);
        resultCount.setText(results.size() + " kissaa löytyi");
    }

    private void clearFilters() {
        nameOrBreed.clear();
        name.clear();
        breed.clear();
        color.clear();
        gender.clear();
        arrivalFrom.clear();
        arrivalTo.clear();
        tagName.clear();
        tagCategory.clear();
        runSearch();
    }

    private String trimOrNull(String s) {
        if (s == null || s.isBlank())
            return null;
        return s.trim();
    }
}
