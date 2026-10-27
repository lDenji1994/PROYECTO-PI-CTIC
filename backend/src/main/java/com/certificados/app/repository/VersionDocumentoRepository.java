package com.certificados.app.repository;

import com.certificados.app.model.VersionDocumento;
import com.certificados.app.dto.VersionDatosDTO;
import com.certificados.app.dto.VersionResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VersionDocumentoRepository
        extends JpaRepository<VersionDocumento, Integer> {

    List<VersionDocumento> findByIdDocumentoAcademico(
            Integer idDocumentoAcademico
    );

    List<VersionDocumento> findByPeriodo(String periodo);

    /** Todas las versiones SIN el binario, del periodo mas reciente al mas antiguo. */
    @Query("select new com.certificados.app.dto.VersionResumenDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.nombreArchivo, v.fechaCarga, "
            + "v.creditos, v.descripcion, v.idProgramaDisena) "
            + "from VersionDocumento v order by v.periodo desc, v.fechaCarga desc, v.id desc")
    List<VersionResumenDTO> listarResumen();

    /** Una version SIN el binario (para no cargar el archivo en memoria). */
    @Query("select new com.certificados.app.dto.VersionResumenDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.nombreArchivo, v.fechaCarga, "
            + "v.creditos, v.descripcion, v.idProgramaDisena) "
            + "from VersionDocumento v where v.id = :id")
    Optional<VersionResumenDTO> buscarResumen(@Param("id") Integer id);

    long countByIdDocumentoAcademico(Integer idDocumentoAcademico);

    /** ¿Algun documento cargado indica este programa como el que diseña el curso? */
    boolean existsByIdProgramaDisena(Integer idProgramaDisena);

    /** Ruta del archivo de una version en la carpeta de archivos (para borrarlo). */
    @Query("select v.rutaArchivo from VersionDocumento v where v.id = :id")
    String rutaArchivo(@Param("id") Integer id);

    /** Borra la version directamente (el archivo se borra aparte de la carpeta). */
    @Modifying
    @Query("delete from VersionDocumento v where v.id = :id")
    void eliminarPorId(@Param("id") Integer id);

    /** Datos del curso (sin el binario) de las versiones de varios documentos. */
    @Query("select new com.certificados.app.dto.VersionDatosDTO("
            + "v.id, v.idDocumentoAcademico, v.periodo, v.fechaCarga, v.creditos, "
            + "v.horasTeoricas, v.horasPracticas, v.horasLaboratorio, v.horasIndependientes, "
            + "v.descripcion, v.resumen) "
            + "from VersionDocumento v where v.idDocumentoAcademico in :idsDocumentos")
    List<VersionDatosDTO> listarDatosPorDocumentos(@Param("idsDocumentos") Collection<Integer> idsDocumentos);

    /** Textos largos de una version (sin el binario), para el certificado COMPLETO. */
    @Query("select new com.certificados.app.dto.VersionTextosDTO("
            + "v.id, v.proposito, v.justificacion, v.requisitos, v.modalidades) "
            + "from VersionDocumento v where v.id = :id")
    Optional<com.certificados.app.dto.VersionTextosDTO> buscarTextos(@Param("id") Integer id);
}
