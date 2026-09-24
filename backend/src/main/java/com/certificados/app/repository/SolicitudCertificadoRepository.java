package com.certificados.app.repository;

import com.certificados.app.model.EstadoSolicitudCertificado;
import com.certificados.app.model.SolicitudCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudCertificadoRepository
        extends JpaRepository<SolicitudCertificado, Integer> {

    List<SolicitudCertificado> findByEstado(
            EstadoSolicitudCertificado estado
    );

    List<SolicitudCertificado> findByIdEstudiante(
            Integer idEstudiante
    );
}

