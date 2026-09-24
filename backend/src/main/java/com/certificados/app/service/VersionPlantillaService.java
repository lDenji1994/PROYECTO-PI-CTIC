package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.repository.PlantillaCertificadoRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VersionPlantillaService {

    private final VersionPlantillaRepository repository;
    private final PlantillaCertificadoRepository plantillaRepository;

    public VersionPlantillaService(
            VersionPlantillaRepository repository,
            PlantillaCertificadoRepository plantillaRepository) {

        this.repository = repository;
        this.plantillaRepository = plantillaRepository;
    }

    public List<VersionPlantilla> listarTodos() {
        return repository.findAll();
    }

    public VersionPlantilla buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Versión de plantilla no encontrada con id "
                                        + id
                        ));
    }

    public List<VersionPlantilla> listarPorPlantilla(
            Integer idPlantillaCertificado) {

        if (!plantillaRepository.existsById(
                idPlantillaCertificado)) {

            throw new ResourceNotFoundException(
                    "Plantilla no encontrada con id "
                            + idPlantillaCertificado
            );
        }

        return repository.findByIdPlantillaCertificado(
                idPlantillaCertificado
        );
    }

    public VersionPlantilla crear(
            String versionFormato,
            Integer idPlantillaCertificado) {

        if (versionFormato == null
                || versionFormato.isBlank()) {

            throw new BusinessException(
                    "La versión de formato es obligatoria"
            );
        }

        if (idPlantillaCertificado == null) {
            throw new BusinessException(
                    "La plantilla es obligatoria"
            );
        }

        if (!plantillaRepository.existsById(
                idPlantillaCertificado)) {

            throw new ResourceNotFoundException(
                    "Plantilla no encontrada con id "
                            + idPlantillaCertificado
            );
        }

        if (repository
                .findByIdPlantillaCertificadoAndVersionFormato(
                        idPlantillaCertificado,
                        versionFormato
                )
                .isPresent()) {

            throw new BusinessException(
                    "Ya existe la versión "
                            + versionFormato
                            + " para esta plantilla"
            );
        }

        VersionPlantilla version =
                new VersionPlantilla();

        version.setVersionFormato(versionFormato);
        version.setIdPlantillaCertificado(
                idPlantillaCertificado
        );

        return repository.save(version);
    }
}