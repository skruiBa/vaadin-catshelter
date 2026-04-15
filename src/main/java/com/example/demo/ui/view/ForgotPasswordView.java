package com.example.demo.ui.view;

import com.example.demo.service.PasswordResetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("forgot-password")
@PageTitle("Unohtuiko salasana | CatShelter")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("Salasanan palautus");

        EmailField emailField = new EmailField("Sahkoposti");
        emailField.setWidth("320px");

        Button sendButton = new Button("Laheta palautuslinkki", e -> {
            passwordResetService.requestPasswordReset(emailField.getValue());
            Notification note = Notification.show(
                    "Jos osoite loytyy jarjestelmasta, lahetimme palautuslinkin.",
                    3500,
                    Notification.Position.MIDDLE);
            note.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setWidth("320px");

        Anchor backToLogin = new Anchor("/login", "Takaisin kirjautumiseen");

        add(title, emailField, sendButton, backToLogin);
    }
}
