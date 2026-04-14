package com.example.demo.ui.view;

import com.example.demo.entity.AdoptionApplication;
import com.example.demo.entity.AdoptionStatus;
import com.example.demo.entity.Cat;
import com.example.demo.repository.AdoptionApplicationRepository;
import com.example.demo.repository.CatRepository;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "adoptioanomukset", layout = MainLayout.class)
@PageTitle("Adoptioanomukset | CatShelter")
@PermitAll
public class AdoptionApplicationsView extends VerticalLayout {

    private final AdoptionApplicationRepository adoptionRepo;
    private final CatRepository catRepository;

    private final Grid<AdoptionApplication> grid = new Grid<>(AdoptionApplication.class, false);

    public AdoptionApplicationsView(AdoptionApplicationRepository adoptionRepo, CatRepository catRepository) {
        this.adoptionRepo = adoptionRepo;
        this.catRepository = catRepository;

        setSizeFull();
        setPadding(true);

        Button addBtn = new Button("Uusi anomus", VaadinIcon.PLUS.create(), e -> openForm(new AdoptionApplication()));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(new H2("Adoptioanomukset 📋"), addBtn, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        // 1:N relaatio — kissan nimi näkyy gridissä
        grid.addColumn(app -> app.getCat() != null ? app.getCat().getName() : "–")
                .setHeader("Kissa").setSortable(true).setAutoWidth(true);
        grid.addColumn(AdoptionApplication::getApplicantName).setHeader("Hakijan nimi").setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(AdoptionApplication::getApplicantEmail).setHeader("Sähköposti").setAutoWidth(true);
        grid.addColumn(AdoptionApplication::getApplicationDate).setHeader("Hakupäivä").setSortable(true)
                .setAutoWidth(true);
        grid.addComponentColumn(app -> {
            Span badge = new Span(app.getStatus() != null ? app.getStatus().name() : "");
            badge.getElement().getThemeList().add("badge");
            if (app.getStatus() == AdoptionStatus.APPROVED)
                badge.getElement().getThemeList().add("success");
            else if (app.getStatus() == AdoptionStatus.REJECTED)
                badge.getElement().getThemeList().add("error");
            else if (app.getStatus() == AdoptionStatus.IN_REVIEW)
                badge.getElement().getThemeList().add("contrast");
            return badge;
        }).setHeader("Status").setAutoWidth(true);
        grid.addColumn(AdoptionApplication::getMessage).setHeader("Viesti").setAutoWidth(true);

        grid.addComponentColumn(app -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openForm(app));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(app));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Toiminnot").setAutoWidth(true);
    }

    private void openForm(AdoptionApplication app) {
        Dialog dialog = new Dialog();
        dialog.setWidth("580px");
        dialog.setHeaderTitle(app.getId() == null ? "Uusi anomus" : "Muokkaa anomusta");

        BeanValidationBinder<AdoptionApplication> binder = new BeanValidationBinder<>(AdoptionApplication.class);

        ComboBox<Cat> catCombo = new ComboBox<>("Kissa");
        catCombo.setItems(catRepository.findAll());
        catCombo.setItemLabelGenerator(Cat::getName);
        catCombo.setValue(app.getCat());
        catCombo.setRequired(true);

        TextField nameField = new TextField("Hakijan nimi");
        EmailField emailField = new EmailField("Sähköposti");
        DatePicker dateField = new DatePicker("Hakupäivä");
        ComboBox<AdoptionStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(AdoptionStatus.values());
        statusCombo.setItemLabelGenerator(AdoptionStatus::name);
        TextArea messageField = new TextArea("Viesti");

        binder.forField(nameField).bind("applicantName");
        binder.forField(emailField).bind("applicantEmail");
        binder.forField(dateField).bind("applicationDate");
        binder.forField(statusCombo).bind("status");
        binder.forField(messageField).bind("message");
        binder.readBean(app);

        FormLayout form = new FormLayout(catCombo, dateField, nameField, emailField, statusCombo, messageField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        form.setColspan(messageField, 2);

        Button saveBtn = new Button("Tallenna", e -> {
            if (catCombo.getValue() == null) {
                showError("Valitse kissa.");
                return;
            }
            try {
                binder.writeBean(app);
                app.setCat(catCombo.getValue());
                adoptionRepo.save(app);
                dialog.close();
                refreshGrid();
                showSuccess("Anomus tallennettu!");
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

    private void confirmDelete(AdoptionApplication app) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Poista anomus?");
        confirm.setText("Poistetaanko hakijan \"" + app.getApplicantName() + "\" anomus?");
        confirm.setCancelable(true);
        confirm.setCancelText("Peruuta");
        confirm.setConfirmText("Poista");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            adoptionRepo.deleteById(app.getId());
            refreshGrid();
            showSuccess("Anomus poistettu.");
        });
        confirm.open();
    }

    private void refreshGrid() {
        grid.setItems(adoptionRepo.findAll());
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
