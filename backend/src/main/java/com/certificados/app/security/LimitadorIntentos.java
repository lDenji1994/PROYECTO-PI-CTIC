package com.certificados.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freno contra "adivinar" contrasenas: despues de varios intentos fallidos
 * seguidos, ese usuario queda bloqueado unos minutos PARA EL EQUIPO (IP)
 * desde donde se hicieron los intentos. Asi nadie puede dejar bloqueado al
 * administrador desde otro computador solo escribiendo mal su contrasena.
 *
 * Se guarda en memoria (se reinicia al reiniciar el servidor), suficiente
 * para una aplicacion de un solo servidor.
 *
 * SE PUEDE MODIFICAR en application.properties:
 *   app.seguridad.intentos-maximos   (por defecto 5)
 *   app.seguridad.minutos-bloqueo    (por defecto 5)
 */
@Component
public class LimitadorIntentos {

    private record Estado(int fallos, Instant bloqueadoHasta) {
    }

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();
    private final int intentosMaximos;
    private final Duration bloqueo;

    public LimitadorIntentos(
            @Value("${app.seguridad.intentos-maximos:5}") int intentosMaximos,
            @Value("${app.seguridad.minutos-bloqueo:5}") int minutosBloqueo) {
        this.intentosMaximos = Math.max(1, intentosMaximos);
        this.bloqueo = Duration.ofMinutes(Math.max(1, minutosBloqueo));
    }

    /** Minutos que faltan para poder intentar de nuevo (0 = no esta bloqueado). */
    public long minutosBloqueado(String usuario, String ip) {
        Estado estado = estados.get(clave(usuario, ip));
        if (estado == null || estado.bloqueadoHasta() == null) {
            return 0;
        }
        long segundos = Duration.between(Instant.now(), estado.bloqueadoHasta()).getSeconds();
        if (segundos <= 0) {
            estados.remove(clave(usuario, ip));
            return 0;
        }
        return (segundos + 59) / 60;
    }

    public void registrarFallo(String usuario, String ip) {
        estados.merge(clave(usuario, ip), new Estado(1, null), (anterior, nuevo) -> {
            int fallos = anterior.fallos() + 1;
            return new Estado(fallos, fallos >= intentosMaximos ? Instant.now().plus(bloqueo) : null);
        });
        // Evita que el mapa crezca sin limite si alguien prueba miles de nombres
        // (se conservan los bloqueos que siguen activos)
        if (estados.size() > 10_000) {
            Instant ahora = Instant.now();
            estados.values().removeIf(e -> e.bloqueadoHasta() == null || e.bloqueadoHasta().isBefore(ahora));
        }
    }

    public void registrarExito(String usuario, String ip) {
        estados.remove(clave(usuario, ip));
    }

    private static String clave(String usuario, String ip) {
        return (usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT)) + "|" + (ip == null ? "" : ip);
    }
}
