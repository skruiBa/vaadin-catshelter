package com.example.demo.ui.view;

import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

/**
 * Kustomoitu 403-sivu tilanteisiin, joissa käyttäjällä ei ole oikeuksia
 * näkymään.
 */
@Route(value = "access-denied", layout = MainLayout.class)
@PageTitle("Ei käyttöoikeutta | CatShelter")
@PermitAll
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    public AccessDeniedView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("Ei käyttöoikeutta");
        Paragraph message = new Paragraph("Sinulla ei ole oikeuksia tälle sivulle.");

        Button home = new Button("Etusivulle", VaadinIcon.HOME.create(), e -> getUI().ifPresent(ui -> ui.navigate("")));
        home.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button login = new Button("Kirjautumiseen", VaadinIcon.SIGN_IN.create(),
                e -> getUI().ifPresent(ui -> ui.navigate("login")));
        login.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(home, login);
        actions.setSpacing(true);

        add(title, message, actions);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
