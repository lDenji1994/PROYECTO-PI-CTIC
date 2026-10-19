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
    private final ActividadService actividadService;

    public ProgramaService(ProgramaRepository programaRepository,
                           ActividadService actividadService) {
        this.programaRepository = programaRepository;
        this.actividadService = actividadService;
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
        programa.setEscuela(textoMembrete(dto.getEscuela(), "La escuela"));
        programa.setFacultad(textoMembrete(dto.getFacultad(), "La facultad"));

        Programa guardado = programaRepository.save(programa);

        // Bitacora: queda en la "Actividad reciente" del Dashboard
        actividadService.registrar(ActividadService.TABLA_PROGRAMAS,
                ActividadService.CREAR_PROGRAMA,
                (guardado.getCodigo() != null ? guardado.getCodigo() + " - " : "") + guardado.getNombre());

        return toDTO(guardado);
    }

    /**
     * Cambia el nombre del programa y la escuela / facultad que salen en el
     * membrete de sus certificados. El codigo no se cambia.
     */
    public ProgramaDTO actualizar(Integer id, ProgramaDTO dto) {
        Programa programa = obtenerEntidad(id);
        String nombre = dto.getNombre() == null ? "" : dto.getNombre().trim().replaceAll("\\s+", " ");
        if (nombre.isEmpty()) {
            throw new BusinessException("El nombre del programa es obligatorio.");
        }
        programa.setNombre(nombre);
        programa.setEscuela(textoMembrete(dto.getEscuela(), "La escuela"));
        programa.setFacultad(textoMembrete(dto.getFacultad(), "La facultad"));
        Programa guardado = programaRepository.save(programa);
        actividadService.registrar(ActividadService.TABLA_PROGRAMAS,
                ActividadService.ACTUALIZAR_PROGRAMA,
                (guardado.getCodigo() != null ? guardado.getCodigo() + " - " : "") + guardado.getNombre());
        return toDTO(guardado);
    }

    /** Texto opcional del membrete: vacio = null (se usa el valor general). */
    private static String textoMembrete(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");
        if (limpio.length() > 150) {
            throw new BusinessException(campo + " admite máximo 150 caracteres.");
        }
        return limpio;
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
        dto.setEscuela(programa.getEscuela());
        dto.setFacultad(programa.getFacultad());

        return dto;
    }
}
