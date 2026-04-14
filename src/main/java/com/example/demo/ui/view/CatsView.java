package com.example.demo.ui.view;

import com.example.demo.entity.Cat;
import com.example.demo.entity.Gender;
import com.example.demo.entity.Tag;
import com.example.demo.repository.CatRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "kissat", layout = MainLayout.class)
@PageTitle("Kissat | CatShelter")
@PermitAll
public class CatsView extends VerticalLayout {

    private final CatRepository catRepository;
    private final TagRepository tagRepository;

    private final Grid<Cat> grid = new Grid<>(Cat.class, false);
    private Cat currentCat;

    public CatsView(CatRepository catRepository, TagRepository tagRepository) {
        this.catRepository = catRepository;
        this.tagRepository = tagRepository;

        setSizeFull();
        setPadding(true);
        // Vaatimus 2a: addClassName – kytkee näkymäkohtaisen CSS:n (.cats-view)
        addClassName("cats-view");
        // Vaatimus 2b: getStyle().set()
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        Button addBtn = new Button("Uusi kissa", VaadinIcon.PLUS.create(), e -> openForm(new Cat()));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Anchor exportCsvLink = createCsvExportLink();

        configureGrid();

        HorizontalLayout actions = new HorizontalLayout(addBtn, exportCsvLink);
        actions.setAlignItems(Alignment.CENTER);

        add(new H2("Kissat 🐱"), actions, grid);
        refreshGrid();
    }

    private Anchor createCsvExportLink() {
        StreamResource resource = new StreamResource("kissat.csv",
                () -> new ByteArrayInputStream(generateCatsCsv().getBytes(StandardCharsets.UTF_8)));

        Anchor exportCsvLink = new Anchor(resource, "");
        exportCsvLink.getElement().setAttribute("download", true);

        Button exportBtn = new Button("Vie CSV", VaadinIcon.DOWNLOAD.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        exportCsvLink.add(exportBtn);

        return exportCsvLink;
    }

    private String generateCatsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nimi,Rotu,Syntymaaika,Sukupuoli,Vari,Paino,Saapunut,Tagit\n");

        for (Cat cat : catRepository.findAll()) {
            String tags = cat.getTags() == null ? ""
                    : cat.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.joining("|"));

            sb.append(csvEscape(cat.getName())).append(',')
                    .append(csvEscape(cat.getBreed())).append(',')
                    .append(csvEscape(cat.getBirthDate() == null ? "" : cat.getBirthDate().toString())).append(',')
                    .append(csvEscape(cat.getGender() == null ? "" : cat.getGender().name())).append(',')
                    .append(csvEscape(cat.getColor())).append(',')
                    .append(csvEscape(cat.getWeight() == null ? "" : cat.getWeight().toString())).append(',')
                    .append(csvEscape(cat.getArrivalDate() == null ? "" : cat.getArrivalDate().toString())).append(',')
                    .append(csvEscape(tags))
                    .append('\n');
        }

        return sb.toString();
    }

    private String csvEscape(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private void configureGrid() {
        grid.setSizeFull();
        // Vaatimus 2c: addThemeVariants
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addColumn(Cat::getName).setHeader("Nimi").setSortable(true).setAutoWidth(true);
        grid.addColumn(Cat::getBreed).setHeader("Rotu").setSortable(true).setAutoWidth(true);
        grid.addColumn(Cat::getBirthDate).setHeader("Syntymäaika").setAutoWidth(true);
        grid.addColumn(cat -> cat.getGender() != null ? cat.getGender().name() : "").setHeader("Sukupuoli")
                .setAutoWidth(true);
        grid.addColumn(Cat::getColor).setHeader("Väri").setAutoWidth(true);
        grid.addColumn(Cat::getWeight).setHeader("Paino (kg)").setAutoWidth(true);
        grid.addColumn(Cat::getArrivalDate).setHeader("Saapunut").setAutoWidth(true);
        // Tagit M:N relaatio näkyvillä
        grid.addComponentColumn(cat -> {
            HorizontalLayout tags = new HorizontalLayout();
            tags.setSpacing(true);
            cat.getTags().forEach(tag -> {
                Span badge = new Span(tag.getName());
                badge.getElement().getThemeList().add("badge");
                badge.getStyle().set("background-color", tag.getColorCode());
                badge.getStyle().set("color", "white");
                tags.add(badge);
            });
            return tags;
        }).setHeader("Tagit").setAutoWidth(true);

        grid.addComponentColumn(cat -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openForm(cat));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(cat));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Toiminnot").setAutoWidth(true);
    }

    private void openForm(Cat cat) {
        this.currentCat = cat;
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle(cat.getId() == null ? "Uusi kissa" : "Muokkaa kissaa");

        BeanValidationBinder<Cat> binder = new BeanValidationBinder<>(Cat.class);

        TextField nameField = new TextField("Nimi");
        TextField breedField = new TextField("Rotu");
        DatePicker birthDateField = new DatePicker("Syntymäaika");
        Select<Gender> genderSelect = new Select<>();
        genderSelect.setLabel("Sukupuoli");
        genderSelect.setItems(Gender.values());
        genderSelect.setItemLabelGenerator(Gender::name);
        TextField colorField = new TextField("Väri");
        NumberField weightField = new NumberField("Paino (kg)");
        weightField.setMin(0.1);
        weightField.setMax(20.0);
        DatePicker arrivalDateField = new DatePicker("Saapumispäivä");
        TextArea descriptionField = new TextArea("Kuvaus");
        TextField imageUrlField = new TextField("Kuvan URL");

        List<Tag> allTags = tagRepository.findAll();
        MultiSelectComboBox<Tag> tagsField = new MultiSelectComboBox<>("Tagit");
        tagsField.setItems(allTags);
        tagsField.setItemLabelGenerator(Tag::getName);
        if (cat.getTags() != null) {
            tagsField.setValue(cat.getTags());
        }

        binder.forField(nameField).bind("name");
        binder.forField(breedField).bind("breed");
        binder.forField(birthDateField).bind("birthDate");
        binder.forField(genderSelect).bind("gender");
        binder.forField(colorField).bind("color");
        binder.forField(weightField).bind("weight");
        binder.forField(arrivalDateField).bind("arrivalDate");
        binder.forField(descriptionField).bind("description");
        binder.forField(imageUrlField).bind("imageUrl");

        binder.readBean(cat);

        FormLayout form = new FormLayout(nameField, breedField, birthDateField, genderSelect,
                colorField, weightField, arrivalDateField, imageUrlField, descriptionField, tagsField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(descriptionField, 2);
        form.setColspan(tagsField, 2);
        form.setColspan(imageUrlField, 2);

        Button saveBtn = new Button("Tallenna", e -> {
            try {
                binder.writeBean(cat);
                cat.setTags(tagsField.getValue());
                catRepository.save(cat);
                dialog.close();
                refreshGrid();
                showSuccess("Kissa tallennettu!");
            } catch (ValidationException ex) {
                showError("Tarkista lomakkeen tiedot.");
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Peruuta", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void confirmDelete(Cat cat) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Poista kissa?");
        confirm.setText("Haluatko varmasti poistaa kissan \"" + cat.getName() + "\"? Toimintoa ei voi perua.");
        confirm.setCancelable(true);
        confirm.setCancelText("Peruuta");
        confirm.setConfirmText("Poista");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            catRepository.deleteById(cat.getId());
            refreshGrid();
            showSuccess("Kissa poistettu.");
        });
        confirm.open();
    }

    private void refreshGrid() {
        grid.setItems(catRepository.findAll());
    }

    private void showSuccess(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
