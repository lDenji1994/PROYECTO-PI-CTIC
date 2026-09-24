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

    boolean existsByIdSolicitudCertificadoAndIdAsignatura(
            Integer idSolicitudCertificado,
            Integer idAsignatura
    );
}
