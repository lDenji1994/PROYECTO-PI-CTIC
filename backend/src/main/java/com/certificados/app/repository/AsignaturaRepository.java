package com.certificados.app.repository;

import com.certificados.app.model.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsignaturaRepository
        extends JpaRepository<Asignatura, Integer> {

    Optional<Asignatura> findByCodigo(String codigo);
}
