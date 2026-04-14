package com.example.demo.ui.view;

import com.example.demo.repository.AdoptionApplicationRepository;
import com.example.demo.repository.CatRepository;
import com.example.demo.repository.HealthRecordRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.ui.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Etusivu | CatShelter")
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    public DashboardView(CatRepository catRepo,
            HealthRecordRepository hrRepo,
            AdoptionApplicationRepository adoptionRepo,
            TagRepository tagRepo) {

        // Vaatimus 2b: getStyle().set() – konttinen tyyli
        getStyle().set("padding", "var(--lumo-space-l)");

        // Otsikko
        H2 title = new H2("🐱 CatShelter – Etusivu");
        // Vaatimus 2a: addClassName
        title.addClassName("dashboard-title");
        // Vaatimus 2b: getStyle() myös otsikolla
        title.getStyle().set("color", "var(--lumo-primary-color)");
        title.getStyle().set("margin-bottom", "0");

        Paragraph subtitle = new Paragraph("Kissatarhan hallintajärjestelmä");
        // Vaatimus 4 – LumoUtility: TextColor (1/5)
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE);

        // ── Tilastokorttirivistö ──────────────────────────────────
        FlexLayout cards = new FlexLayout();
        cards.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cards.getStyle().set("gap", "var(--lumo-space-m)");
        // Vaatimus 4 – LumoUtility: Margin (2/5)
        cards.addClassName(LumoUtility.Margin.Vertical.LARGE);

        cards.add(
                statCard("🐈 Kissat", String.valueOf(catRepo.count()), "#2d2876"),
                statCard("🏥 Terveyskortit", String.valueOf(hrRepo.count()), "#5f75d6"),
                statCard("📋 Anomukset", String.valueOf(adoptionRepo.count()), "#d89594"),
                statCard("🏷️ Tagit", String.valueOf(tagRepo.count()), "#2d2876"));

        // ── Info-osio ─────────────────────────────────────────────
        Div infoBox = new Div();
        // Vaatimus 4 – LumoUtility: Background (3/5), Padding (4/5), BorderRadius (5/5)
        infoBox.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.LARGE);
        // Vaatimus 2b: getStyle() myös info-boksi
        infoBox.getStyle().set("max-width", "600px");
        infoBox.getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");

        H3 infoTitle = new H3("Tervetuloa!");
        // Vaatimus 4 – LumoUtility: FontSize lisäksi
        infoTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.NONE);

        Paragraph infoText = new Paragraph(
                "Tässä järjestelmässä hallitset kissatarhan kissoja, terveyskortteja, " +
                        "adoptioanomuksia ja tageja. Navigoi vasemman reunan valikosta.");
        infoText.addClassName(LumoUtility.TextColor.BODY);

        infoBox.add(infoTitle, infoText);

        add(title, subtitle, cards, infoBox);
    }

    /**
     * Tilastokortti – käyttää stat-card CSS-luokkaa (hover/transition
     * styles.css:ssä).
     * Vaatimus 2a: addClassName, Vaatimus 2b: getStyle().set(), Vaatimus 2c:
     * theme-variant ei tässä
     */
    private Div statCard(String label, String value, String accentColor) {
        Div card = new Div();

        // Vaatimus 2a: addClassName
        card.addClassName("stat-card");
        // Vaatimus 4 – BoxShadow LumoUtility
        card.addClassNames(LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.LARGE);

        // Vaatimus 2b: getStyle().set()
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border-left", "4px solid " + accentColor);
        card.getStyle().set("min-width", "160px");
        card.getStyle().set("flex", "1 1 160px");

        Span icon = new Span(label);
        icon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        icon.getStyle().set("display", "block");

        Span num = new Span(value);
        num.getStyle().set("font-size", "2.5rem");
        num.getStyle().set("font-weight", "800");
        num.getStyle().set("color", accentColor);
        num.getStyle().set("line-height", "1.1");

        card.add(icon, num);
        return card;
    }
}
