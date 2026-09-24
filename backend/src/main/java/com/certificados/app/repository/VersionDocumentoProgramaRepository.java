package com.certificados.app.repository;

import com.certificados.app.model.VersionDocumentoPrograma;
import com.certificados.app.model.VersionDocumentoProgramaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionDocumentoProgramaRepository
        extends JpaRepository<VersionDocumentoPrograma, VersionDocumentoProgramaId> {

    List<VersionDocumentoPrograma> findByIdVersionDocumento(
            Integer idVersionDocumento);

    List<VersionDocumentoPrograma> findByIdPrograma(
            Integer idPrograma);
}