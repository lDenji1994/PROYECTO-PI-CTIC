package com.certificados.app.repository;

import com.certificados.app.model.CampoPlantilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampoPlantillaRepository
        extends JpaRepository<CampoPlantilla, Integer> {

    List<CampoPlantilla> findByIdVersionPlantilla(
            Integer idVersionPlantilla
    );

    List<CampoPlantilla> findByIdSeccionPlantilla(
            Integer idSeccionPlantilla
    );

    Optional<CampoPlantilla>
    findByIdVersionPlantillaAndCodigoCampo(
            Integer idVersionPlantilla,
            String codigoCampo
    );
}