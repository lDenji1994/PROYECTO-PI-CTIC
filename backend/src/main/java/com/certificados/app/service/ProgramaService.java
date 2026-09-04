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

@Service
@Transactional
public class ProgramaService {

    private final ProgramaRepository programaRepository;

    public ProgramaService(ProgramaRepository programaRepository) {
        this.programaRepository = programaRepository;
    }

    public List<ProgramaDTO> listarTodos() {
        return programaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProgramaDTO buscarPorId(Long id) {
        return toDTO(obtenerEntidad(id));
    }

    public ProgramaDTO crear(ProgramaDTO dto) {
        if (programaRepository.existsByCodigo(dto.getCodigo())) {
            throw new BusinessException("Ya existe un programa registrado con el código " + dto.getCodigo());
        }

        Programa programa = new Programa();
        programa.setNombre(dto.getNombre());
        programa.setCodigo(dto.getCodigo());
        programa.setDescripcion(dto.getDescripcion());

        return toDTO(programaRepository.save(programa));
    }

    private Programa obtenerEntidad(Long id) {
        return programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado con id " + id));
    }

    private ProgramaDTO toDTO(Programa p) {
        ProgramaDTO dto = new ProgramaDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setCodigo(p.getCodigo());
        dto.setDescripcion(p.getDescripcion());
        return dto;
    }
}