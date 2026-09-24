package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.SeccionPlantilla;
import com.certificados.app.repository.SeccionPlantillaRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SeccionPlantillaService {

    private final SeccionPlantillaRepository repository;
    private final VersionPlantillaRepository versionPlantillaRepository;

    public SeccionPlantillaService(
            SeccionPlantillaRepository repository,
            VersionPlantillaRepository versionPlantillaRepository) {

        this.repository = repository;
        this.versionPlantillaRepository =
                versionPlantillaRepository;
    }

    public List<SeccionPlantilla> listarTodos() {
        return repository.findAll();
    }

    public SeccionPlantilla buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sección de plantilla no encontrada con id "
                                        + id
                        ));
    }

    public List<SeccionPlantilla> listarPorVersionPlantilla(
            Integer idVersionPlantilla) {

        validarVersion(idVersionPlantilla);

        return repository.findByIdVersionPlantilla(
                idVersionPlantilla
        );
    }

    public SeccionPlantilla crear(
            String codigoSeccion,
            String nombreSeccion,
            Boolean repetible,
            Integer orden,
            Integer idVersionPlantilla) {

        if (codigoSeccion == null
                || codigoSeccion.isBlank()) {

            throw new BusinessException(
                    "El código de la sección es obligatorio"
            );
        }

        if (nombreSeccion == null
                || nombreSeccion.isBlank()) {

            throw new BusinessException(
                    "El nombre de la sección es obligatorio"
            );
        }

        if (orden == null || orden <= 0) {
            throw new BusinessException(
                    "El orden de la sección debe ser mayor que cero"
            );
        }

        if (idVersionPlantilla == null) {
            throw new BusinessException(
                    "La versión de plantilla es obligatoria"
            );
        }

        validarVersion(idVersionPlantilla);

        if (repository
                .findByIdVersionPlantillaAndCodigoSeccion(
                        idVersionPlantilla,
                        codigoSeccion
                )
                .isPresent()) {

            throw new BusinessException(
                    "Ya existe una sección con el código "
                            + codigoSeccion
                            + " para esta versión"
            );
        }

        SeccionPlantilla seccion =
                new SeccionPlantilla();

        seccion.setCodigoSeccion(
                codigoSeccion
        );

        seccion.setNombreSeccion(
                nombreSeccion
        );

        seccion.setRepetible(
                repetible != null && repetible
        );

        seccion.setOrden(orden);

        seccion.setIdVersionPlantilla(
                idVersionPlantilla
        );

        return repository.save(seccion);
    }

    private void validarVersion(
            Integer idVersionPlantilla) {

        if (!versionPlantillaRepository.existsById(
                idVersionPlantilla)) {

            throw new ResourceNotFoundException(
                    "Versión de plantilla no encontrada con id "
                            + idVersionPlantilla
            );
        }
    }
}