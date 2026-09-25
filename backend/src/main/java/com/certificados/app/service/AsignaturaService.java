package com.certificados.app.service;

import com.certificados.app.dto.AsignaturaDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Asignaturas con codigo de MATERIA + codigo de CURSO (ej. FION 0001).
 *
 * En BD se guardan juntos en c_codigo como "MATERIA CURSO".
 *
 * SE PUEDE MODIFICAR: las expresiones regulares si la universidad usa
 * otro patron de codigos (hoy: materia 2-6 letras/digitos, curso 2-8).
 * NO MODIFICAR: el separador (un espacio). Cambiarlo haria que las
 * asignaturas ya guardadas no se separen bien.
 */
@Service
@Transactional
public class AsignaturaService {

    public static final String SEPARADOR = " ";

    private static final Pattern PATRON_MATERIA = Pattern.compile("^[A-Z0-9]{2,6}$");
    private static final Pattern PATRON_CURSO = Pattern.compile("^[A-Z0-9]{2,8}$");

    private final AsignaturaRepository repository;
    private final ActividadService actividadService;

    public AsignaturaService(AsignaturaRepository repository, ActividadService actividadService) {
        this.repository = repository;
        this.actividadService = actividadService;
    }

    @Transactional(readOnly = true)
    public List<AsignaturaDTO> listarTodas() {
        return repository.findAll().stream()
                .map(AsignaturaService::aDTO)
                .sorted(Comparator.comparing(AsignaturaDTO::getCodigo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public AsignaturaDTO buscarPorId(Integer id) {
        return aDTO(obtener(id));
    }

    @Transactional(readOnly = true)
    public AsignaturaDTO buscarPorCodigo(String codigo) {
        return repository.findByCodigo(normalizarCodigoCompleto(codigo))
                .map(AsignaturaService::aDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una asignatura con código " + codigo));
    }

    public Asignatura obtener(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + id));
    }

    public AsignaturaDTO crear(AsignaturaDTO dto) {
        String materia;
        String curso;

        if (dto.getCodigoMateria() != null && !dto.getCodigoMateria().isBlank()) {
            materia = limpiar(dto.getCodigoMateria());
            curso = limpiar(dto.getCodigoCurso());
        } else if (dto.getCodigo() != null && !dto.getCodigo().isBlank()) {
            String[] partes = separar(dto.getCodigo());
            materia = partes[0];
            curso = partes[1];
        } else {
            throw new BusinessException("El código de materia es obligatorio");
        }

        if (!PATRON_MATERIA.matcher(materia).matches()) {
            throw new BusinessException("Código de materia inválido: use de 2 a 6 letras o dígitos (ej. FION)");
        }
        if (!PATRON_CURSO.matcher(curso).matches()) {
            throw new BusinessException("Código de curso inválido: use de 2 a 8 letras o dígitos (ej. 0001)");
        }

        String codigo = materia + SEPARADOR + curso;
        if (repository.existsByCodigo(codigo)) {
            throw new BusinessException("Ya existe una asignatura con el código " + codigo);
        }

        Asignatura asignatura = new Asignatura();
        asignatura.setCodigo(codigo);
        asignatura.setNombre(dto.getNombre().trim());
        Asignatura guardada = repository.save(asignatura);

        actividadService.registrar(ActividadService.TABLA_ASIGNATURAS, ActividadService.CREAR_ASIGNATURA,
                codigo + " - " + guardada.getNombre());

        return aDTO(guardada);
    }

    /* ---------------------------- utilidades ---------------------------- */

    public static AsignaturaDTO aDTO(Asignatura a) {
        String[] partes = separar(a.getCodigo());
        return new AsignaturaDTO(a.getId(), partes[0], partes[1], a.getCodigo(), a.getNombre());
    }

    /** "fion-0001", "FION 0001", "FION0001"... -> ["FION", "0001"]. */
    public static String[] separar(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        String[] partes = c.split("[\\s\\-_/]+", 2);
        if (partes.length == 2) {
            return new String[]{partes[0], partes[1].replaceAll("\\s+", "")};
        }
        // Sin separador: letras iniciales = materia, resto = curso (FION0001)
        var m = Pattern.compile("^([A-Z]+)([0-9][A-Z0-9]*)$").matcher(c);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return new String[]{c, ""};
    }

    private static String normalizarCodigoCompleto(String codigo) {
        String[] p = separar(codigo);
        return p[1].isEmpty() ? p[0] : p[0] + SEPARADOR + p[1];
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }
}
