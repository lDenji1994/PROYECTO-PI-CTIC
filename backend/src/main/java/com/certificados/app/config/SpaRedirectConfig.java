package com.certificados.app.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Redirige rutas no-API hacia index.html para que el enrutador
 * de Angular (rutas del lado del cliente) funcione al recargar
 * la pagina en una ruta profunda, ej: /estudiantes/3
 */
@Controller
public class SpaRedirectConfig {

    @RequestMapping(value = {
            "/",
            "/{path:[^\\.]*}",
            "/{path:^(?!api)[^\\.]*}/**"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
