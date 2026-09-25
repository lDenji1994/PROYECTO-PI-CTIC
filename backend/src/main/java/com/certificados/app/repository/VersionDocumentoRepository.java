package com.certificados.app.repository;

import com.certificados.app.model.VersionDocumento;
import com.certificados.app.dto.VersionResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VersionDocumentoRepository
        extends JpaRepository<VersionDocumento, Integer> {

    List<VersionDocumento> findByIdDocumentoAcademico(
            Integer idDocumentoAcademico
    );

    List<VersionDocumento> findByPeriodo(String periodo);

    /** Todas las versiones SIN el binario, de la mas reciente a la mas antigua. */
    @Query("select new com.certificados.app.dto.VersionResumenDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.nombreArchivo, v.fechaCarga) "
            + "from VersionDocumento v order by v.fechaCarga desc, v.id desc")
    List<VersionResumenDTO> listarResumen();
}
