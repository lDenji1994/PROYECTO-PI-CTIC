package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.CampoPlantilla;
import com.certificados.app.repository.CampoPlantillaRepository;
import com.certificados.app.repository.SeccionPlantillaRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CampoPlantillaService {

    private final CampoPlantillaRepository repository;
    private final VersionPlantillaRepository versionPlantillaRepository;
    private final SeccionPlantillaRepository seccionPlantillaRepository;

    public CampoPlantillaService(
            CampoPlantillaRepository repository,
            VersionPlantillaRepository versionPlantillaRepository,
            SeccionPlantillaRepository seccionPlantillaRepository) {

        this.repository = repository;
        this.versionPlantillaRepository =
                versionPlantillaRepository;
        this.seccionPlantillaRepository =
                seccionPlantillaRepository;
    }

    public List<CampoPlantilla> listarTodos() {
        return repository.findAll();
    }

    public CampoPlantilla buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Campo de plantilla no encontrado con id "
                                        + id
                        ));
    }

    public List<CampoPlantilla> listarPorVersionPlantilla(
            Integer idVersionPlantilla) {

        validarVersion(idVersionPlantilla);

        return repository.findByIdVersionPlantilla(
                idVersionPlantilla
        );
    }

    public List<CampoPlantilla> listarPorSeccionPlantilla(
            Integer idSeccionPlantilla) {

        if (!seccionPlantillaRepository.existsById(
                idSeccionPlantilla)) {

            throw new ResourceNotFoundException(
                    "Sección de plantilla no encontrada con id "
                            + idSeccionPlantilla
            );
        }

        return repository.findByIdSeccionPlantilla(
                idSeccionPlantilla
        );
    }

    public CampoPlantilla crear(
            String codigoCampo,
            String nombreCampo,
            CampoPlantilla.TipoDato tipoDato,
            String fuente,
            CampoPlantilla.MetodoObtencion metodoObtencion,
            Boolean repetible,
            String reglaObtencion,
            Integer idVersionPlantilla,
            Integer idSeccionPlantilla) {

        if (codigoCampo == null || codigoCampo.isBlank()) {
            throw new BusinessException(
                    "El código del campo es obligatorio"
            );
        }

        if (nombreCampo == null || nombreCampo.isBlank()) {
            throw new BusinessException(
                    "El nombre del campo es obligatorio"
            );
        }

        if (tipoDato == null) {
            throw new BusinessException(
                    "El tipo de dato es obligatorio"
            );
        }

        if (fuente == null || fuente.isBlank()) {
            throw new BusinessException(
                    "La fuente del campo es obligatoria"
            );
        }

        if (metodoObtencion == null) {
            throw new BusinessException(
                    "El método de obtención es obligatorio"
            );
        }

        if (idVersionPlantilla == null) {
            throw new BusinessException(
                    "La versión de plantilla es obligatoria"
            );
        }

        validarVersion(idVersionPlantilla);

        if (idSeccionPlantilla != null) {

            var seccion = seccionPlantillaRepository
                    .findById(idSeccionPlantilla)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sección de plantilla no encontrada con id "
                                            + idSeccionPlantilla
                            ));

            if (!seccion.getIdVersionPlantilla()
                    .equals(idVersionPlantilla)) {

                throw new BusinessException(
                        "La sección no pertenece a la versión "
                                + "de plantilla indicada"
                );
            }
        }

        if (repository
                .findByIdVersionPlantillaAndCodigoCampo(
                        idVersionPlantilla,
                        codigoCampo
                )
                .isPresent()) {

            throw new BusinessException(
                    "Ya existe un campo con el código "
                            + codigoCampo
                            + " para esta versión"
            );
        }

        CampoPlantilla campo =
                new CampoPlantilla();

        campo.setCodigoCampo(codigoCampo);
        campo.setNombreCampo(nombreCampo);
        campo.setTipoDato(tipoDato);
        campo.setFuente(fuente);
        campo.setMetodoObtencion(metodoObtencion);
        campo.setRepetible(
                repetible != null && repetible
        );
        campo.setReglaObtencion(reglaObtencion);
        campo.setIdVersionPlantilla(
                idVersionPlantilla
        );
        campo.setIdSeccionPlantilla(
                idSeccionPlantilla
        );

        return repository.save(campo);
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