package com.example.demo;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setEmail("admin@catshelter.fi");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("Käyttäjä");
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("superuser")) {
            AppUser superUser = new AppUser();
            superUser.setUsername("superuser");
            superUser.setEmail("super@catshelter.fi");
            superUser.setPasswordHash(passwordEncoder.encode("super123"));
            superUser.setFirstName("Super");
            superUser.setLastName("Käyttäjä");
            superUser.setRoles(Set.of(Role.ROLE_SUPER));
            superUser.setEnabled(true);
            userRepository.save(superUser);
        }

        if (!userRepository.existsByUsername("user")) {
            AppUser user = new AppUser();
            user.setUsername("user");
            user.setEmail("user@catshelter.fi");
            user.setPasswordHash(passwordEncoder.encode("user123"));
            user.setFirstName("Tavallinen");
            user.setLastName("Käyttäjä");
            user.setRoles(Set.of(Role.ROLE_USER));
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}
