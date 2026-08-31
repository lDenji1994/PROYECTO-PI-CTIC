package com.certificados.app.service;

import com.certificados.app.dto.EstudianteDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Estudiante;
import com.certificados.app.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<EstudianteDTO> listarTodos() {
        return estudianteRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstudianteDTO buscarPorId(Long id) {
        Estudiante estudiante = obtenerEntidad(id);
        return toDTO(estudiante);
    }

    public EstudianteDTO crear(EstudianteDTO dto) {
        if (estudianteRepository.existsByCodigoEstudiantil(dto.getCodigoEstudiantil())) {
            throw new BusinessException("Ya existe un estudiante con ese codigo estudiantil");
        }
        Estudiante estudiante = toEntity(dto);
        estudiante.setId(null);
        return toDTO(estudianteRepository.save(estudiante));
    }

    public EstudianteDTO actualizar(Long id, EstudianteDTO dto) {
        Estudiante estudiante = obtenerEntidad(id);
        estudiante.setNombres(dto.getNombres());
        estudiante.setApellidos(dto.getApellidos());
        estudiante.setEmail(dto.getEmail());
        estudiante.setProgramaAcademico(dto.getProgramaAcademico());
        return toDTO(estudianteRepository.save(estudiante));
    }

    public void eliminar(Long id) {
        Estudiante estudiante = obtenerEntidad(id);
        estudianteRepository.delete(estudiante);
    }

    private Estudiante obtenerEntidad(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con id " + id));
    }

    private EstudianteDTO toDTO(Estudiante e) {
        return new EstudianteDTO(e.getId(), e.getCodigoEstudiantil(), e.getNombres(),
                e.getApellidos(), e.getEmail(), e.getProgramaAcademico());
    }

    private Estudiante toEntity(EstudianteDTO dto) {
        Estudiante e = new Estudiante();
        e.setId(dto.getId());
        e.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        e.setNombres(dto.getNombres());
        e.setApellidos(dto.getApellidos());
        e.setEmail(dto.getEmail());
        e.setProgramaAcademico(dto.getProgramaAcademico());
        return e;
    }
}