package com.certificados.app.repository;

import com.certificados.app.model.SeccionPlantilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeccionPlantillaRepository
        extends JpaRepository<SeccionPlantilla, Integer> {

    List<SeccionPlantilla> findByIdVersionPlantilla(
            Integer idVersionPlantilla
    );

    Optional<SeccionPlantilla>
    findByIdVersionPlantillaAndCodigoSeccion(
            Integer idVersionPlantilla,
            String codigoSeccion
    );
}