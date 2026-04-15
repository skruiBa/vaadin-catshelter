package com.example.demo.ui.view;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProfileImageStorageService;
import com.example.demo.service.RegistrationNotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Route("register")
@PageTitle("Rekisteröidy | CatShelter")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStorageService profileImageStorageService;
    private final RegistrationNotificationService registrationNotificationService;

    public RegisterView(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileImageStorageService profileImageStorageService,
            RegistrationNotificationService registrationNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileImageStorageService = profileImageStorageService;
        this.registrationNotificationService = registrationNotificationService;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField usernameField = new TextField("Käyttäjänimi");
        usernameField.setWidth("300px");

        TextField firstNameField = new TextField("Etunimi");
        firstNameField.setWidth("300px");

        TextField lastNameField = new TextField("Sukunimi");
        lastNameField.setWidth("300px");

        EmailField emailField = new EmailField("Sähköposti");
        emailField.setWidth("300px");

        PasswordField passwordField = new PasswordField("Salasana");
        passwordField.setWidth("300px");

        PasswordField confirmField = new PasswordField("Salasana uudelleen");
        confirmField.setWidth("300px");

        // Oikea tiedoston upload + tallennus rekisteroinnin yhteydessa
        MemoryBuffer imageBuffer = new MemoryBuffer();
        Upload imageUpload = new Upload(imageBuffer);
        imageUpload.setWidth("300px");
        imageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp", "image/gif");
        imageUpload.setMaxFileSize(2_000_000);
        imageUpload.setMaxFiles(1);

        Paragraph uploadInfo = new Paragraph("Profiilikuva (valinnainen, max 2 MB)");
        uploadInfo.getStyle().set("margin", "0");

        AtomicReference<byte[]> uploadedImage = new AtomicReference<>();
        AtomicReference<String> uploadedFileName = new AtomicReference<>();

        imageUpload.addSucceededListener(event -> {
            try (InputStream in = imageBuffer.getInputStream()) {
                uploadedImage.set(in.readAllBytes());
                uploadedFileName.set(event.getFileName());
                Notification.show("Kuva ladattu onnistuneesti.", 2000, Notification.Position.BOTTOM_START);
            } catch (IOException ex) {
                uploadedImage.set(null);
                uploadedFileName.set(null);
                showError("Kuvan kasittely epaonnistui.");
            }
        });
        imageUpload.addFileRejectedListener(event -> showError("Upload epaonnistui: " + event.getErrorMessage()));

        Button registerBtn = new Button("Rekisteröidy", e -> {
            String username = usernameField.getValue().trim();
            String email = emailField.getValue().trim();
            String password = passwordField.getValue();
            String confirm = confirmField.getValue();
            String firstName = firstNameField.getValue().trim();
            String lastName = lastNameField.getValue().trim();

            if (username.isBlank() || email.isBlank() || password.isBlank()
                    || firstName.isBlank() || lastName.isBlank()) {
                showError("Täytä kaikki kentät.");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Salasanat eivät täsmää.");
                return;
            }
            if (password.length() < 6) {
                showError("Salasanan on oltava vähintään 6 merkkiä.");
                return;
            }
            if (userRepository.existsByUsername(username)) {
                showError("Käyttäjänimi on jo käytössä.");
                return;
            }
            if (userRepository.existsByEmail(email)) {
                showError("Sähköposti on jo rekisteröity.");
                return;
            }

            AppUser user = new AppUser();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRoles(Set.of(Role.ROLE_USER));
            user.setEnabled(true);

            if (uploadedImage.get() != null && uploadedFileName.get() != null) {
                try {
                    String imagePath = profileImageStorageService.storeProfileImage(
                            username,
                            uploadedFileName.get(),
                            uploadedImage.get());
                    user.setProfileImagePath(imagePath);
                } catch (IOException ex) {
                    showError("Profiilikuvan tallennus epaonnistui: " + ex.getMessage());
                    return;
                }
            }

            userRepository.save(user);
            registrationNotificationService.notifyAdminNewUser(user);

            Notification.show("Rekisteröityminen onnistui! Voit nyt kirjautua sisään.",
                    3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("login");
        });
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidth("300px");

        Anchor loginLink = new Anchor("/login", "Jo tili? Kirjaudu sisään");
        loginLink.getStyle().set("margin-top", "var(--lumo-space-s)");

        add(
                new H2("🐱 Rekisteröidy"),
                usernameField,
                firstNameField,
                lastNameField,
                emailField,
                passwordField,
                confirmField,
                uploadInfo,
                imageUpload,
                registerBtn,
                loginLink);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
