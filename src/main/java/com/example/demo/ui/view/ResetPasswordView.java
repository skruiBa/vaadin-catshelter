package com.example.demo.ui.view;

import com.example.demo.service.PasswordResetService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("reset-password")
@PageTitle("Uusi salasana | CatShelter")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordResetService passwordResetService;
    private String token;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("Aseta uusi salasana");

        PasswordField newPassword = new PasswordField("Uusi salasana");
        newPassword.setWidth("320px");
        PasswordField confirmPassword = new PasswordField("Uusi salasana uudelleen");
        confirmPassword.setWidth("320px");

        Button saveButton = new Button("Vaihda salasana", e -> {
            if (token == null || token.isBlank()) {
                showError("Palautuslinkki puuttuu tai on virheellinen.");
                return;
            }
            if (!newPassword.getValue().equals(confirmPassword.getValue())) {
                showError("Salasanat eivat tasmää.");
                return;
            }
            if (newPassword.getValue().length() < 6) {
                showError("Salasanan on oltava vahintaan 6 merkkia.");
                return;
            }

            boolean ok = passwordResetService.resetPassword(token, newPassword.getValue());
            if (!ok) {
                showError("Palautuslinkki on vanhentunut tai virheellinen.");
                return;
            }

            Notification n = Notification.show("Salasana vaihdettu onnistuneesti.", 3000,
                    Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("login");
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidth("320px");

        Anchor backToLogin = new Anchor("/login", "Takaisin kirjautumiseen");

        add(title, newPassword, confirmPassword, saveButton, backToLogin);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = event.getLocation().getQueryParameters().getSingleParameter("token").orElse("");
        if (token.isBlank() || !passwordResetService.isResetTokenValid(token)) {
            Notification n = Notification.show("Palautuslinkki on virheellinen tai vanhentunut.", 3500,
                    Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 3500, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
