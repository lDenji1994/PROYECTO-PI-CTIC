package com.certificados.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permite acceso libre a todo (frontend, estáticos y endpoints /api/**)
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails usuarioAuxiliar = User.builder()
                .username("auxiliar")
                .password("{noop}auxiliar123")
                .roles("AUXILIAR")
                .build();

        UserDetails usuarioAdmin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMINISTRADOR")
                .build();

        return new InMemoryUserDetailsManager(usuarioAuxiliar, usuarioAdmin);
    }
}