package com.example.demo.ui.view;

import com.example.demo.entity.Tag;
import com.example.demo.repository.CatRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "tagit", layout = MainLayout.class)
@PageTitle("Tagit | CatShelter")
@RolesAllowed("ROLE_ADMIN")
public class TagsView extends VerticalLayout {

    private final TagRepository tagRepository;
    private final CatRepository catRepository;
    private final Grid<Tag> grid = new Grid<>(Tag.class, false);

    public TagsView(TagRepository tagRepository, CatRepository catRepository) {
        this.tagRepository = tagRepository;
        this.catRepository = catRepository;

        setSizeFull();
        setPadding(true);

        Button addBtn = new Button("Uusi tagi", VaadinIcon.PLUS.create(), e -> openForm(new Tag()));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(new H2("Tagit 🏷️"), addBtn, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addComponentColumn(tag -> {
            Span badge = new Span(tag.getName());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle().set("background-color", tag.getColorCode() != null ? tag.getColorCode() : "#888");
            badge.getStyle().set("color", "white");
            return badge;
        }).setHeader("Nimi").setAutoWidth(true);
        grid.addColumn(Tag::getCategory).setHeader("Kategoria").setSortable(true).setAutoWidth(true);
        grid.addColumn(Tag::getDescription).setHeader("Kuvaus").setAutoWidth(true);
        grid.addColumn(Tag::getColorCode).setHeader("Värikoodi").setAutoWidth(true);
        grid.addColumn(Tag::getIcon).setHeader("Ikoni").setAutoWidth(true);
        // M:N relaatio — kissojen lukumäärä näkyy
        grid.addColumn(tag -> tag.getCats() != null ? tag.getCats().size() + " kissaa" : "0 kissaa")
                .setHeader("Kissat").setAutoWidth(true);

        grid.addComponentColumn(tag -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openForm(tag));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(tag));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Toiminnot").setAutoWidth(true);
    }

    private void openForm(Tag tag) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle(tag.getId() == null ? "Uusi tagi" : "Muokkaa tagia");

        BeanValidationBinder<Tag> binder = new BeanValidationBinder<>(Tag.class);

        TextField nameField = new TextField("Nimi");
        TextField categoryField = new TextField("Kategoria");
        TextField colorCodeField = new TextField("Värikoodi (hex)");
        colorCodeField.setPlaceholder("#4CAF50");
        TextField iconField = new TextField("Ikoni");
        TextArea descriptionField = new TextArea("Kuvaus");

        binder.forField(nameField).bind("name");
        binder.forField(categoryField).bind("category");
        binder.forField(colorCodeField).bind("colorCode");
        binder.forField(iconField).bind("icon");
        binder.forField(descriptionField).bind("description");
        binder.readBean(tag);

        FormLayout form = new FormLayout(nameField, categoryField, colorCodeField, iconField, descriptionField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("350px", 2));
        form.setColspan(descriptionField, 2);

        Button saveBtn = new Button("Tallenna", e -> {
            try {
                binder.writeBean(tag);
                tagRepository.save(tag);
                dialog.close();
                refreshGrid();
                showSuccess("Tagi tallennettu!");
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

    private void confirmDelete(Tag tag) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Poista tagi?");
        int catCount = tag.getCats() != null ? tag.getCats().size() : 0;
        confirm.setText("Poistetaanko tagi \"" + tag.getName() + "\"?" +
                (catCount > 0 ? " Sillä on " + catCount + " kissaa." : ""));
        confirm.setCancelable(true);
        confirm.setCancelText("Peruuta");
        confirm.setConfirmText("Poista");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            // Poista viittaukset kissoilta ennen tagin poistoa
            tag.getCats().forEach(cat -> {
                cat.getTags().remove(tag);
                catRepository.save(cat);
            });
            tagRepository.deleteById(tag.getId());
            refreshGrid();
            showSuccess("Tagi poistettu.");
        });
        confirm.open();
    }

    private void refreshGrid() {
        grid.setItems(tagRepository.findAll());
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
