package com.certificados.app.repository;

import com.certificados.app.model.TipoDocumentoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoDocumentoAcademicoRepository
        extends JpaRepository<TipoDocumentoAcademico, Integer> {

    Optional<TipoDocumentoAcademico> findByCodigo(String codigo);
}
