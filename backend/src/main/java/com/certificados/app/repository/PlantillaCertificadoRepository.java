package com.certificados.app.repository;

import com.certificados.app.model.PlantillaCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantillaCertificadoRepository
        extends JpaRepository<PlantillaCertificado, Integer> {

    List<PlantillaCertificado>
    findByIdTipoCertificado(Integer idTipoCertificado);

    Optional<PlantillaCertificado>
    findByNombre(String nombre);
}
