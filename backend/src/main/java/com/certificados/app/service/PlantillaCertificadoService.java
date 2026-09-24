package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.PlantillaCertificado;
import com.certificados.app.repository.PlantillaCertificadoRepository;
import com.certificados.app.repository.TipoCertificadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlantillaCertificadoService {

    private final PlantillaCertificadoRepository repository;
    private final TipoCertificadoRepository tipoCertificadoRepository;

    public PlantillaCertificadoService(
            PlantillaCertificadoRepository repository,
            TipoCertificadoRepository tipoCertificadoRepository) {

        this.repository = repository;
        this.tipoCertificadoRepository =
                tipoCertificadoRepository;
    }

    public List<PlantillaCertificado> listarTodos() {
        return repository.findAll();
    }

    public PlantillaCertificado buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plantilla no encontrada con id " + id
                        ));
    }

    public PlantillaCertificado crear(
            String nombre,
            Integer idTipoCertificado) {

        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException(
                    "El nombre de la plantilla es obligatorio"
            );
        }

        if (idTipoCertificado == null) {
            throw new BusinessException(
                    "El tipo de certificado es obligatorio"
            );
        }

        if (!tipoCertificadoRepository.existsById(
                idTipoCertificado)) {

            throw new ResourceNotFoundException(
                    "Tipo de certificado no encontrado con id "
                            + idTipoCertificado
            );
        }

        if (repository.findByNombre(nombre).isPresent()) {
            throw new BusinessException(
                    "Ya existe una plantilla con el nombre "
                            + nombre
            );
        }

        PlantillaCertificado plantilla =
                new PlantillaCertificado();

        plantilla.setNombre(nombre);
        plantilla.setIdTipoCertificado(
                idTipoCertificado
        );

        return repository.save(plantilla);
    }
}