package com.example.demo.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(createHeader());
        addToDrawer(createNavigation(), createFooter());
    }

    private HorizontalLayout createHeader() {
        H1 title = new H1("🐱 CatShelter");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)");
        title.getStyle().set("margin", "0");

        boolean loggedIn = isLoggedIn();

        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "Tuntematon";
        if (!loggedIn) {
            username = "Vieras";
        }

        Span userSpan = new Span(VaadinIcon.USER.create(), new Span(" " + username));
        userSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Button authBtn;
        if (loggedIn) {
            authBtn = new Button("Kirjaudu ulos", VaadinIcon.SIGN_OUT.create(),
                    e -> UI.getCurrent().getPage().setLocation("/logout"));
        } else {
            authBtn = new Button("Kirjaudu sisään", VaadinIcon.SIGN_IN.create(),
                    e -> UI.getCurrent().navigate("login"));
        }
        authBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);

        HorizontalLayout userArea = new HorizontalLayout(userSpan, authBtn);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        header.add(userArea);

        return header;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        SideNavItem dashboard = new SideNavItem("Etusivu", "", VaadinIcon.HOME.create());
        SideNavItem cats = new SideNavItem("Kissat", "kissat", VaadinIcon.STAR.create());
        SideNavItem health = new SideNavItem("Terveyskortit", "terveyskortit", VaadinIcon.HEART.create());
        SideNavItem adoptions = new SideNavItem("Adoptioanomukset", "adoptioanomukset", VaadinIcon.FILE_TEXT.create());
        SideNavItem tags = new SideNavItem("Tagit", "tagit", VaadinIcon.TAG.create());
        SideNavItem search = new SideNavItem("Haku", "haku", VaadinIcon.SEARCH.create());
        SideNavItem notes = new SideNavItem("Muistiinpanot", "muistiinpanot", VaadinIcon.EDIT.create());
        SideNavItem history = new SideNavItem("Historia", "historia", VaadinIcon.TIME_BACKWARD.create());

        nav.addItem(dashboard);

        if (isLoggedIn()) {
            nav.addItem(cats, adoptions, search, notes, history);
        }
        if (hasRole("ROLE_SUPER") || hasRole("ROLE_USER")) {
            nav.addItem(health);
        }
        if (hasRole("ROLE_ADMIN")) {
            nav.addItem(tags);
        }

        return nav;
    }

    private boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }

    private Footer createFooter() {
        Span name = new Span("© 2026 CatShelter");
        Paragraph info = new Paragraph("Kissatarhan hallintajärjestelmä");
        info.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        info.getStyle().set("margin", "0");

        VerticalLayout content = new VerticalLayout(name, info);
        content.setPadding(false);
        content.setSpacing(false);

        Footer footer = new Footer(content);
        footer.getStyle().set("padding", "var(--lumo-space-m)");
        footer.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");
        return footer;
    }
}
