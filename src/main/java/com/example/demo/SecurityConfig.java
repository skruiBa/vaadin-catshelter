package com.example.demo;

import com.example.demo.security.AppUserDetailsService;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final AppUserDetailsService userDetailsService;
    private final Environment environment;

    public SecurityConfig(AppUserDetailsService userDetailsService, Environment environment) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/register", "/logout", "/oauth2/**", "/login/**", "/forgot-password",
                        "/reset-password")
                .permitAll());
        if (oauthConfigured("github")) {
            http.oauth2Login(oauth -> {
            });
        }
        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        http.userDetailsService(userDetailsService);
        super.configure(http);
        setLoginView(http, "/login");
    }

    private boolean oauthConfigured(String provider) {
        String clientId = environment
                .getProperty("spring.security.oauth2.client.registration." + provider + ".client-id", "");
        return clientId != null && !clientId.isBlank();
    }
}
