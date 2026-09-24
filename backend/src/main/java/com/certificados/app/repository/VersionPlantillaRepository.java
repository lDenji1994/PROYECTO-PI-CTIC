package com.certificados.app.repository;

import com.certificados.app.model.VersionPlantilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VersionPlantillaRepository
        extends JpaRepository<VersionPlantilla, Integer> {

    List<VersionPlantilla> findByIdPlantillaCertificado(
            Integer idPlantillaCertificado
    );

    List<VersionPlantilla> findByIdPlantillaCertificadoOrderByFechaCargaDesc(
            Integer idPlantillaCertificado
    );

    Optional<VersionPlantilla> findByIdPlantillaCertificadoAndVersionFormato(
            Integer idPlantillaCertificado,
            String versionFormato
    );
}