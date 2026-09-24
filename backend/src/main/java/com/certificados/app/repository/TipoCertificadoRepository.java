package com.certificados.app.repository;

import com.certificados.app.model.TipoCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoCertificadoRepository
        extends JpaRepository<TipoCertificado, Integer> {

    Optional<TipoCertificado> findByNombre(String nombre);
}
