package com.certificados.app.repository;

import com.certificados.app.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Long> {
    boolean existsByCodigo(String codigo);
    Optional<Programa> findByCodigo(String codigo);
}