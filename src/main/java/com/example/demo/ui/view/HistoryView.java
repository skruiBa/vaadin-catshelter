package com.example.demo.ui.view;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import com.example.demo.entity.Cat;
import com.example.demo.repository.CatRepository;
import com.example.demo.service.CatHistoryService;
import com.example.demo.service.CatHistoryService.CatHistoryRow;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "historia", layout = MainLayout.class)
@PageTitle("Historia | CatShelter")
@RolesAllowed({ "ROLE_ADMIN", "ROLE_SUPER", "ROLE_USER" })
public class HistoryView extends VerticalLayout {

    private final CatHistoryService catHistoryService;
    private final CatRepository catRepository;

    private final ComboBox<Cat> catSelect = new ComboBox<>("Valitse kissa");
    private final Grid<CatHistoryRow> grid = new Grid<>(CatHistoryRow.class, false);

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public HistoryView(CatHistoryService catHistoryService, CatRepository catRepository) {
        this.catHistoryService = catHistoryService;
        this.catRepository = catRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Entiteettihistoria (Cat)");

        catSelect.setItems(catRepository.findAll().stream().sorted(Comparator.comparing(Cat::getName)).toList());
        catSelect.setItemLabelGenerator(c -> c.getName() + " (#" + c.getId() + ")");
        catSelect.setWidth("340px");

        Button loadBtn = new Button("Lataa historia", e -> loadHistory());
        loadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout controls = new HorizontalLayout(catSelect, loadBtn);
        controls.setAlignItems(Alignment.END);

        configureGrid();

        add(title, controls, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        grid.addColumn(CatHistoryRow::revision).setHeader("Rev").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::action).setHeader("Tapahtuma").setAutoWidth(true);
        grid.addColumn(r -> r.timestamp() == null ? "" : dtf.format(r.timestamp()))
                .setHeader("Aika").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::name).setHeader("Nimi").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::breed).setHeader("Rotu").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::color).setHeader("Vari").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::weight).setHeader("Paino").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::arrivalDate).setHeader("Saapunut").setAutoWidth(true);
        grid.addColumn(CatHistoryRow::tags).setHeader("Tagit").setAutoWidth(true).setFlexGrow(1);
    }

    private void loadHistory() {
        Cat cat = catSelect.getValue();
        if (cat == null || cat.getId() == null) {
            grid.setItems();
            return;
        }
        grid.setItems(catHistoryService.findCatHistory(cat.getId()));
    }
}
