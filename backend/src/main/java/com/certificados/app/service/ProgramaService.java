package com.certificados.app.service;

import com.certificados.app.dto.ProgramaDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Programa;
import com.certificados.app.repository.ProgramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio del módulo de Programas Académicos.
 *
 * Gestiona el registro y consulta de programas académicos.
 * Los datos administrados corresponden directamente a ProgramasS
 * de la base de datos.
 */
@Service
@Transactional
public class ProgramaService {

    private final ProgramaRepository programaRepository;

    public ProgramaService(ProgramaRepository programaRepository) {
        this.programaRepository = programaRepository;
    }

    /**
     * Consulta todos los programas académicos registrados.
     */
    public List<ProgramaDTO> listarTodos() {
        return programaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Consulta un programa académico por su identificador.
     */
    public ProgramaDTO buscarPorId(Integer id) {
        return toDTO(obtenerEntidad(id));
    }

    /**
     * Registra un nuevo programa académico.
     *
     * El código es opcional de acuerdo con la estructura de la BD.
     * Si se proporciona, no puede repetirse.
     */
    public ProgramaDTO crear(ProgramaDTO dto) {

        if (dto.getCodigo() != null
                && programaRepository.existsByCodigo(dto.getCodigo())) {

            throw new BusinessException(
                    "Ya existe un programa registrado con el código "
                            + dto.getCodigo()
            );
        }

        Programa programa = new Programa();

        programa.setCodigo(dto.getCodigo());
        programa.setNombre(dto.getNombre());

        return toDTO(programaRepository.save(programa));
    }

    /**
     * Obtiene la entidad correspondiente al identificador indicado.
     */
    private Programa obtenerEntidad(Integer id) {
        return programaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Programa no encontrado con id " + id
                        )
                );
    }

    /**
     * Convierte la entidad JPA a DTO.
     */
    private ProgramaDTO toDTO(Programa programa) {

        ProgramaDTO dto = new ProgramaDTO();

        dto.setId(programa.getId());
        dto.setCodigo(programa.getCodigo());
        dto.setNombre(programa.getNombre());

        return dto;
    }
}
