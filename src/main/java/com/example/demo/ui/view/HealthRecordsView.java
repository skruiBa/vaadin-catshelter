package com.example.demo.ui.view;

import com.example.demo.entity.Cat;
import com.example.demo.entity.HealthRecord;
import com.example.demo.repository.CatRepository;
import com.example.demo.repository.HealthRecordRepository;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "terveyskortit", layout = MainLayout.class)
@PageTitle("Terveyskortit | CatShelter")
@RolesAllowed({ "ROLE_SUPER", "ROLE_USER" })
public class HealthRecordsView extends VerticalLayout {

    private final HealthRecordRepository healthRecordRepository;
    private final CatRepository catRepository;

    private final Grid<HealthRecord> grid = new Grid<>(HealthRecord.class, false);

    public HealthRecordsView(HealthRecordRepository healthRecordRepository, CatRepository catRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.catRepository = catRepository;

        setSizeFull();
        setPadding(true);

        Button addBtn = new Button("Uusi terveyskortti", VaadinIcon.PLUS.create(), e -> openForm(new HealthRecord()));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(new H2("Terveyskortit 🏥"), addBtn, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        // 1:1 relaatio — kissan nimi näkyy gridissä
        grid.addColumn(hr -> hr.getCat() != null ? hr.getCat().getName() : "–")
                .setHeader("Kissa").setSortable(true).setAutoWidth(true);
        grid.addColumn(HealthRecord::getLastCheckup).setHeader("Viimeisin tarkastus").setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(HealthRecord::getVeterinarian).setHeader("Eläinlääkäri").setSortable(true).setAutoWidth(true);
        grid.addColumn(HealthRecord::getWeight).setHeader("Paino (kg)").setAutoWidth(true);
        grid.addColumn(HealthRecord::getVaccinations).setHeader("Rokotukset").setAutoWidth(true);
        grid.addColumn(HealthRecord::getNotes).setHeader("Huomiot").setAutoWidth(true);

        grid.addComponentColumn(hr -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openForm(hr));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(hr));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Toiminnot").setAutoWidth(true);
    }

    private void openForm(HealthRecord record) {
        Dialog dialog = new Dialog();
        dialog.setWidth("580px");
        dialog.setHeaderTitle(record.getId() == null ? "Uusi terveyskortti" : "Muokkaa terveyskorttia");

        BeanValidationBinder<HealthRecord> binder = new BeanValidationBinder<>(HealthRecord.class);

        ComboBox<Cat> catCombo = new ComboBox<>("Kissa");
        catCombo.setItems(catRepository.findAll());
        catCombo.setItemLabelGenerator(Cat::getName);
        catCombo.setValue(record.getCat());
        catCombo.setRequired(true);

        DatePicker lastCheckupField = new DatePicker("Viimeisin tarkastus");
        TextField vetField = new TextField("Eläinlääkäri");
        NumberField weightField = new NumberField("Paino (kg)");
        weightField.setMin(0.1);
        weightField.setMax(20.0);
        TextField vaccinationsField = new TextField("Rokotukset");
        TextArea notesField = new TextArea("Huomiot");

        binder.forField(lastCheckupField).bind("lastCheckup");
        binder.forField(vetField).bind("veterinarian");
        binder.forField(weightField).bind("weight");
        binder.forField(vaccinationsField).bind("vaccinations");
        binder.forField(notesField).bind("notes");
        binder.readBean(record);

        FormLayout form = new FormLayout(catCombo, lastCheckupField, vetField, weightField, vaccinationsField,
                notesField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(notesField, 2);
        form.setColspan(vaccinationsField, 2);

        Button saveBtn = new Button("Tallenna", e -> {
            if (catCombo.getValue() == null) {
                showError("Valitse kissa.");
                return;
            }
            long catId = catCombo.getValue().getId();
            if (record.getId() == null && healthRecordRepository.existsByCat_Id(catId)) {
                showError("Tällä kissalla on jo terveyskortti.");
                return;
            }
            try {
                binder.writeBean(record);
                record.setCat(catCombo.getValue());
                healthRecordRepository.save(record);
                dialog.close();
                refreshGrid();
                showSuccess("Terveyskortti tallennettu!");
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

    private void confirmDelete(HealthRecord record) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Poista terveyskortti?");
        String catName = record.getCat() != null ? record.getCat().getName() : "?";
        confirm.setText("Poistetaanko kissan \"" + catName + "\" terveyskortti?");
        confirm.setCancelable(true);
        confirm.setCancelText("Peruuta");
        confirm.setConfirmText("Poista");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            healthRecordRepository.deleteById(record.getId());
            refreshGrid();
            showSuccess("Terveyskortti poistettu.");
        });
        confirm.open();
    }

    private void refreshGrid() {
        grid.setItems(healthRecordRepository.findAll());
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
