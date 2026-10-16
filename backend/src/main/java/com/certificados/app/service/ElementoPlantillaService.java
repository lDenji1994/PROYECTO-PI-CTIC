package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.CampoPlantilla;
import com.certificados.app.model.ElementoPlantilla;
import com.certificados.app.repository.CampoPlantillaRepository;
import com.certificados.app.repository.ElementoPlantillaRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ElementoPlantillaService {

    private final ElementoPlantillaRepository repository;
    private final VersionPlantillaRepository versionPlantillaRepository;
    private final CampoPlantillaRepository campoPlantillaRepository;

    public ElementoPlantillaService(
            ElementoPlantillaRepository repository,
            VersionPlantillaRepository versionPlantillaRepository,
            CampoPlantillaRepository campoPlantillaRepository) {

        this.repository = repository;
        this.versionPlantillaRepository =
                versionPlantillaRepository;
        this.campoPlantillaRepository =
                campoPlantillaRepository;
    }

    public List<ElementoPlantilla> listarTodos() {
        return repository.findAll();
    }

    public ElementoPlantilla buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Elemento de plantilla no encontrado con id "
                                        + id
                        ));
    }

    public List<ElementoPlantilla> listarPorVersionPlantilla(
            Integer idVersionPlantilla) {

        validarVersion(idVersionPlantilla);

        return repository.findByIdVersionPlantilla(
                idVersionPlantilla
        );
    }

    public List<ElementoPlantilla> listarPorCampoPlantilla(
            Integer idCampoPlantilla) {

        if (!campoPlantillaRepository.existsById(
                idCampoPlantilla)) {

            throw new ResourceNotFoundException(
                    "Campo de plantilla no encontrado con id "
                            + idCampoPlantilla
            );
        }

        return repository.findByIdCampoPlantilla(
                idCampoPlantilla
        );
    }

    public ElementoPlantilla crear(
            ElementoPlantilla.TipoElemento tipoElemento,
            String contenido,
            java.math.BigDecimal posicionX,
            java.math.BigDecimal posicionY,
            java.math.BigDecimal ancho,
            java.math.BigDecimal alto,
            java.math.BigDecimal tamanoFuente,
            String tipoFuente,
            String alineacion,
            Integer orden,
            Integer idVersionPlantilla,
            Integer idCampoPlantilla) {

        if (tipoElemento == null) {
            throw new BusinessException(
                    "El tipo de elemento es obligatorio"
            );
        }

        if (idVersionPlantilla == null) {
            throw new BusinessException(
                    "La versión de plantilla es obligatoria"
            );
        }

        validarVersion(idVersionPlantilla);

        CampoPlantilla campo = null;

        if (idCampoPlantilla != null) {

            campo = campoPlantillaRepository
                    .findById(idCampoPlantilla)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Campo de plantilla no encontrado con id "
                                            + idCampoPlantilla
                            ));

            if (!campo.getIdVersionPlantilla()
                    .equals(idVersionPlantilla)) {

                throw new BusinessException(
                        "El campo no pertenece a la versión "
                                + "de plantilla indicada"
                );
            }
        }

        ElementoPlantilla elemento =
                new ElementoPlantilla();

        elemento.setTipoElemento(tipoElemento);
        elemento.setContenido(contenido);
        elemento.setPosicionX(posicionX);
        elemento.setPosicionY(posicionY);
        elemento.setAncho(ancho);
        elemento.setAlto(alto);
        elemento.setTamanoFuente(tamanoFuente);
        elemento.setTipoFuente(tipoFuente);
        elemento.setAlineacion(alineacion);
        elemento.setOrden(orden);
        elemento.setIdVersionPlantilla(
                idVersionPlantilla
        );
        elemento.setIdCampoPlantilla(
                idCampoPlantilla
        );

        return repository.save(elemento);
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