package com.certificados.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuracion de CORS.
 *
 * El panel normalmente se sirve desde el MISMO servidor (localhost:8080),
 * asi que CORS no interviene. Solo importa si alguien abre el HTML desde
 * otro origen (Live Server, otro puerto...).
 *
 * SE PUEDE MODIFICAR: la lista de origenes en application.properties
 * (app.cors.allowed-origins) o con la variable de entorno CORS_ORIGINS.
 * NO MODIFICAR: la division por comas. Antes se pasaba la lista completa
 * como un unico origen y CORS nunca funcionaba.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origenes = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
