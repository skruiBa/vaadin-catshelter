package com.example.demo.ui.view;

import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "muistiinpanot", layout = MainLayout.class)
@PageTitle("Muistiinpanot | CatShelter")
@RolesAllowed({ "ROLE_ADMIN", "ROLE_SUPER", "ROLE_USER" })
@JavaScript("https://cdn.quilljs.com/1.3.7/quill.min.js")
@StyleSheet("https://cdn.quilljs.com/1.3.7/quill.snow.css")
public class RichTextNotesView extends VerticalLayout {

    private final H2 title = new H2();
    private final Paragraph subtitle = new Paragraph();

    private final ComboBox<String> languageSwitch = new ComboBox<>();
    private final Div editorHost = new Div();
    private final Button saveButton = new Button();
    private final Button clearButton = new Button();
    private final Span status = new Span();

    private final TextArea savedHtmlPreview = new TextArea();

    private String currentLanguage = "FI";

    public RichTextNotesView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        languageSwitch.setItems("FI", "EN");
        languageSwitch.setValue("FI");
        languageSwitch.setLabel("Kieli / Language");
        languageSwitch.setWidth("220px");
        languageSwitch.addValueChangeListener(e -> {
            currentLanguage = e.getValue() == null ? "FI" : e.getValue();
            applyTexts();
            updateQuillPlaceholder();
        });

        editorHost.setWidthFull();
        editorHost.getStyle()
                .set("min-height", "300px")
                .set("background", "white")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-s)");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setIcon(VaadinIcon.CHECK.create());
        saveButton.addClickListener(e -> saveEditorContent());

        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.setIcon(VaadinIcon.ERASER.create());
        clearButton.addClickListener(e -> {
            editorHost.getElement().executeJs("if (this.__quill) { this.__quill.setText(''); }");
            status.setText(isEnglish() ? "Editor cleared." : "Editori tyhjennetty.");
        });

        savedHtmlPreview.setWidthFull();
        savedHtmlPreview.setReadOnly(true);
        savedHtmlPreview.setMinHeight("140px");

        HorizontalLayout actions = new HorizontalLayout(saveButton, clearButton);
        actions.setSpacing(true);

        add(title, subtitle, languageSwitch, editorHost, actions, status, savedHtmlPreview);

        applyTexts();
        initializeQuill();
    }

    private void initializeQuill() {
        editorHost.getElement().executeJs(
                """
                        if (!this.__quillInitialized) {
                          this.innerHTML = '';
                          const editor = document.createElement('div');
                          editor.style.minHeight = '220px';
                          this.appendChild(editor);
                          this.__quill = new window.Quill(editor, {
                            theme: 'snow',
                            placeholder: $0
                          });
                          this.__quillInitialized = true;
                        }
                        """,
                editorPlaceholder());
    }

    private void updateQuillPlaceholder() {
        editorHost.getElement().executeJs(
                "if (this.__quill) { this.__quill.root.dataset.placeholder = $0; }",
                editorPlaceholder());
    }

    private void saveEditorContent() {
        editorHost.getElement()
                .executeJs("return this.__quill ? this.__quill.root.innerHTML : ''; ")
                .then(String.class, html -> {
                    savedHtmlPreview.setValue(html == null ? "" : html);
                    Notification n = Notification.show(
                            isEnglish() ? "Content saved to preview field." : "Sisalto tallennettu esikatseluun.",
                            2500,
                            Notification.Position.BOTTOM_START);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                });
    }

    private void applyTexts() {
        if (isEnglish()) {
            title.setText("Quill Editor Notes");
            subtitle.setText(
                    "External JavaScript component demo + localized UI (EN/FI). Write rich text and save a preview.");
            saveButton.setText("Save Content");
            clearButton.setText("Clear");
            savedHtmlPreview.setLabel("Saved HTML preview");
            status.setText("Language switched to English.");
        } else {
            title.setText("Quill-muistiinpanot");
            subtitle.setText(
                    "Ulkoinen JavaScript-komponentti + FI/EN-lokalisointi. Kirjoita muotoiltua tekstia ja tallenna esikatselu.");
            saveButton.setText("Tallenna sisalto");
            clearButton.setText("Tyhjenna");
            savedHtmlPreview.setLabel("Tallennettu HTML-esikatselu");
            status.setText("Kieli vaihdettu suomeksi.");
        }
    }

    private String editorPlaceholder() {
        return isEnglish() ? "Write your notes here..." : "Kirjoita muistiinpanot tahan...";
    }

    private boolean isEnglish() {
        return "EN".equalsIgnoreCase(currentLanguage);
    }
}
