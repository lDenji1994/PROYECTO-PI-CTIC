package com.certificados.app.controller;

import com.certificados.app.dto.CambioContrasenaDTO;
import com.certificados.app.dto.UsuarioCrearDTO;
import com.certificados.app.dto.UsuarioResumenDTO;
import com.certificados.app.security.UsuarioDetallesService;
import com.certificados.app.security.UsuarioSesion;
import com.certificados.app.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * USUARIOS.
 *
 *   GET   /api/usuarios                  -> cualquier usuario con sesion.
 *                                           El administrador ve todos; los demas, solo los activos.
 *   POST  /api/usuarios                  -> SOLO ADMINISTRADOR: crear usuario
 *   PATCH /api/usuarios/{id}/estado      -> SOLO ADMINISTRADOR: activar / desactivar  (?activo=true|false)
 *   PATCH /api/usuarios/{id}/contrasena  -> SOLO ADMINISTRADOR: asignar contrasena nueva
 *
 * Quien puede llamar a cada ruta se define en SecurityConfig.
 * Ninguna respuesta incluye contrasenas.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResumenDTO> listar() {
        boolean esAdmin = UsuarioDetallesService.actual()
                .map(UsuarioSesion::esAdministrador).orElse(false);
        return service.listar(!esAdmin);
    }

    /** Limites configurados, para que el panel los muestre. */
    @GetMapping("/limites")
    public Map<String, Integer> limites() {
        return Map.of("maxAuxiliares", service.maxAuxiliares());
    }

    @PostMapping
    public ResponseEntity<UsuarioResumenDTO> crear(@RequestBody UsuarioCrearDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResumenDTO cambiarEstado(@PathVariable Integer id, @RequestParam boolean activo) {
        Integer idActual = UsuarioDetallesService.actual().map(UsuarioSesion::getId).orElse(null);
        return service.cambiarEstado(id, activo, idActual);
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<Void> restablecerContrasena(@PathVariable Integer id,
                                                      @RequestBody CambioContrasenaDTO datos) {
        service.restablecerContrasena(id, datos.nueva());
        return ResponseEntity.noContent().build();
    }
}
