package com.certificados.app.repository;

import com.certificados.app.model.ElementoPlantilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElementoPlantillaRepository
        extends JpaRepository<ElementoPlantilla, Integer> {

    List<ElementoPlantilla> findByIdVersionPlantilla(
            Integer idVersionPlantilla);

    List<ElementoPlantilla> findByIdSeccionPlantilla(
            Integer idSeccionPlantilla);

    List<ElementoPlantilla> findByIdCampoPlantilla(
            Integer idCampoPlantilla);
}