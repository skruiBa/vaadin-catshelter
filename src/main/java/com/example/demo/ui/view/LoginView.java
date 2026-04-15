package com.example.demo.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Kirjaudu sisään | CatShelter")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Suomenkielinen lokalisointi
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("CatShelter");
        i18n.getForm().setUsername("Käyttäjänimi");
        i18n.getForm().setPassword("Salasana");
        i18n.getForm().setSubmit("Kirjaudu sisään");
        i18n.getForm().setForgotPassword("Unohtuiko salasana?");
        i18n.getErrorMessage().setTitle("Virheellinen käyttäjänimi tai salasana");
        i18n.getErrorMessage().setMessage("Tarkista käyttäjänimi ja salasana.");
        loginForm.setI18n(i18n);
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.addForgotPasswordListener(e -> UI.getCurrent().navigate("forgot-password"));

        Anchor registerLink = new Anchor("/register", "Rekisteröidy");
        registerLink.getStyle().set("margin-top", "var(--lumo-space-m)");

        Button githubLogin = new Button("Kirjaudu GitHubilla", e -> UI.getCurrent().getPage()
                .setLocation("/oauth2/authorization/github"));
        githubLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(
                new H2("🐱 CatShelter"),
                new Paragraph("Kissatarhan hallintajärjestelmä"),
                loginForm,
                githubLogin,
                registerLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
