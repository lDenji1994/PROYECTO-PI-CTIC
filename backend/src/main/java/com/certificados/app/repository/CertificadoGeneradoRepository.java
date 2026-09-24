package com.certificados.app.repository;

import com.certificados.app.model.CertificadoGenerado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificadoGeneradoRepository
        extends JpaRepository<CertificadoGenerado, Integer> {

    Optional<CertificadoGenerado>
    findByIdSolicitudCertificado(Integer idSolicitudCertificado);
}