package com.certificados.app.repository;

import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.DetalleSolicitudCertificadoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleSolicitudCertificadoRepository
        extends JpaRepository<
                DetalleSolicitudCertificado,
                DetalleSolicitudCertificadoId> {

    List<DetalleSolicitudCertificado>
    findByIdSolicitudCertificado(Integer idSolicitudCertificado);

    /** ¿La asignatura esta en alguna solicitud? (no se puede eliminar si es asi) */
    boolean existsByIdAsignatura(Integer idAsignatura);

    /** Quita las asignaturas de una solicitud (se usa al eliminar la solicitud). */
    void deleteByIdSolicitudCertificado(Integer idSolicitudCertificado);

    boolean existsByIdSolicitudCertificadoAndIdAsignatura(
            Integer idSolicitudCertificado,
            Integer idAsignatura
    );
}
