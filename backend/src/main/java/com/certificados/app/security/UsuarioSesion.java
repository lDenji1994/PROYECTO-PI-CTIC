package com.certificados.app.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * El usuario que inicio sesion, tal como lo guarda Spring Security en la
 * sesion HTTP. Se construye desde la tabla de usuarios (UsuarioDetallesService).
 *
 * rol = nombre del rol en la tabla RolesS: ADMINISTRADOR o AUXILIAR.
 * Spring lo usa como autoridad "ROLE_<rol>" (ver SecurityConfig).
 */
public class UsuarioSesion implements UserDetails {

    private static final long serialVersionUID = 1L;

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_AUXILIAR = "AUXILIAR";

    private final Integer id;
    private final String usuario;
    private final String nombreCompleto;
    private final String rol;
    private final String hashContrasena;
    private final boolean activo;

    public UsuarioSesion(Integer id, String usuario, String nombreCompleto,
                         String rol, String hashContrasena, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.hashContrasena = hashContrasena;
        this.activo = activo;
    }

    public Integer getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getRol() { return rol; }
    public boolean esAdministrador() { return ROL_ADMINISTRADOR.equals(rol); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override public String getPassword() { return hashContrasena; }
    @Override public String getUsername() { return usuario; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return activo; }
}
