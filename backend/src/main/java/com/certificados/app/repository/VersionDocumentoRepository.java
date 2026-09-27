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

    /** Todas las versiones SIN el binario, del periodo mas reciente al mas antiguo. */
    @Query("select new com.certificados.app.dto.VersionResumenDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.nombreArchivo, v.fechaCarga) "
            + "from VersionDocumento v order by v.periodo desc, v.fechaCarga desc, v.id desc")
    List<VersionResumenDTO> listarResumen();
}
