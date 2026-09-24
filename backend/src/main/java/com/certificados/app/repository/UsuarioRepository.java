package com.certificados.app.repository;

import com.certificados.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByUsuario(String usuario);

    boolean existsByCorreo(String correo);
}
