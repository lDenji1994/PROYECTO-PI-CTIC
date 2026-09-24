package com.certificados.app.repository;

import com.certificados.app.model.VersionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VersionDocumentoRepository
        extends JpaRepository<VersionDocumento, Integer> {

    List<VersionDocumento> findByIdDocumentoAcademico(
            Integer idDocumentoAcademico
    );

    List<VersionDocumento> findByPeriodo(String periodo);
}
