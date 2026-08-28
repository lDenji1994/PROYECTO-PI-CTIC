package com.certificados.app.repository;

import com.certificados.app.model.HistorialEstadoCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoCertificadoRepository extends JpaRepository<HistorialEstadoCertificado, Long> {

    // Util para David T. en US-21.03 (implementar consulta de historial):
    // devuelve los cambios de un certificado en orden cronologico.
    List<HistorialEstadoCertificado> findByCertificadoIdOrderByFechaCambioAsc(Long certificadoId);
}
