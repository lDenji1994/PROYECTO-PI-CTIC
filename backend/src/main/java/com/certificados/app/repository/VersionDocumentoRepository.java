package com.certificados.app.repository;

import com.certificados.app.model.VersionDocumento;
import com.certificados.app.dto.VersionDatosDTO;
import com.certificados.app.dto.VersionResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VersionDocumentoRepository
        extends JpaRepository<VersionDocumento, Integer> {

    List<VersionDocumento> findByIdDocumentoAcademico(
            Integer idDocumentoAcademico
    );

    List<VersionDocumento> findByPeriodo(String periodo);

    /** Todas las versiones SIN el binario, del periodo mas reciente al mas antiguo. */
    @Query("select new com.certificados.app.dto.VersionResumenDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.nombreArchivo, v.fechaCarga, "
            + "v.creditos, v.descripcion) "
            + "from VersionDocumento v order by v.periodo desc, v.fechaCarga desc, v.id desc")
    List<VersionResumenDTO> listarResumen();

    /** Datos del curso (sin el binario) de las versiones de varios documentos. */
    @Query("select new com.certificados.app.dto.VersionDatosDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.fechaCarga, v.creditos, "
            + "v.horasTeoricas, v.horasPracticas, v.horasLaboratorio, v.horasIndependientes, "
            + "v.descripcion) "
            + "from VersionDocumento v where v.idDocumentoAcademico in :idsDocumentos")
    List<VersionDatosDTO> listarDatosPorDocumentos(@Param("idsDocumentos") Collection<Integer> idsDocumentos);
}
