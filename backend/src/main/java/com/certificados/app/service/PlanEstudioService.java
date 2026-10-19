package com.certificados.app.service;

import com.certificados.app.dto.PlanEstudioDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.EstadoPlanEstudio;
import com.certificados.app.model.PlanEstudio;
import com.certificados.app.model.Programa;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.PlanEstudioRepository;
import com.certificados.app.repository.ProgramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PLAN DE ESTUDIOS DE CADA PROGRAMA (US-09).
 *
 * Guarda en que semestre de un programa va cada asignatura. Lo usa la
 * solicitud de certificado para pedir uno o varios semestres, o el
 * programa completo, y el certificado para agrupar las asignaturas por
 * semestre.
 *
 * SE PUEDE MODIFICAR: SEMESTRE_MAXIMO (si algun programa tiene mas semestres;
 * cambiar tambien el CHECK de la tabla en la base de datos).
 */
@Service
@Transactional
public class PlanEstudioService {

    /** Semestres posibles: 1 .. SEMESTRE_MAXIMO. */
    public static final int SEMESTRE_MAXIMO = 20;

    /** Largo maximo del nombre o periodo del plan (columna t_plan). */
    public static final int PLAN_MAXIMO = 60;

    private final PlanEstudioRepository planRepository;
    private final ProgramaRepository programaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final ActividadService actividadService;

    public PlanEstudioService(PlanEstudioRepository planRepository,
                              ProgramaRepository programaRepository,
                              AsignaturaRepository asignaturaRepository,
                              ActividadService actividadService) {
        this.planRepository = planRepository;
        this.programaRepository = programaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.actividadService = actividadService;
    }

    /** Asignaturas del plan, ordenadas por semestre y codigo. */
    @Transactional(readOnly = true)
    public List<PlanEstudioDTO> listar(Integer idPrograma) {
        programa(idPrograma);
        List<PlanEstudio> filas = planRepository.findByIdProgramaOrderBySemestreAscIdAsignaturaAsc(idPrograma);
        Map<Integer, Asignatura> asignaturas = asignaturaRepository
                .findAllById(filas.stream().map(PlanEstudio::getIdAsignatura).toList()).stream()
                .collect(Collectors.toMap(Asignatura::getId, Function.identity()));
        return filas.stream()
                .filter(f -> asignaturas.containsKey(f.getIdAsignatura()))
                .map(f -> aDTO(f, asignaturas.get(f.getIdAsignatura())))
                .sorted(Comparator.comparing(PlanEstudioDTO::semestre)
                        .thenComparing(PlanEstudioDTO::codigo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Ids de las asignaturas de los semestres indicados (todos si semestres es null). */
    @Transactional(readOnly = true)
    public List<Integer> asignaturasDe(Integer idPrograma, Collection<Integer> semestres) {
        List<PlanEstudio> filas = semestres == null
                ? planRepository.findByIdProgramaOrderBySemestreAscIdAsignaturaAsc(idPrograma)
                : planRepository.findByIdProgramaAndSemestreIn(idPrograma, semestres);
        return filas.stream().map(PlanEstudio::getIdAsignatura).toList();
    }

    /** Semestre de cada asignatura en el programa (para agrupar el certificado). */
    @Transactional(readOnly = true)
    public Map<Integer, Integer> semestresPorAsignatura(Integer idPrograma) {
        return planRepository.findByIdProgramaOrderBySemestreAscIdAsignaturaAsc(idPrograma).stream()
                .collect(Collectors.toMap(PlanEstudio::getIdAsignatura, PlanEstudio::getSemestre, (a, b) -> a));
    }

    /**
     * Agrega la asignatura al plan, o le cambia los datos si ya estaba.
     *
     * @param plan   nombre o periodo del plan (ej. "Plan 2026"); null = no cambiarlo,
     *               texto vacio = borrarlo
     * @param estado VIGENTE, ANTIGUO o DESACTUALIZADO; null = no cambiarlo
     *               (VIGENTE si la asignatura es nueva en el plan)
     */
    public PlanEstudioDTO guardar(Integer idPrograma, Integer idAsignatura, Integer semestre,
                                  String plan, String estado) {
        Programa programa = programa(idPrograma);
        Asignatura asignatura = asignaturaRepository.findById(idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + idAsignatura));
        validarSemestre(semestre);
        EstadoPlanEstudio estadoPlan = estado == null ? null : estadoPlan(estado);

        PlanEstudio fila = planRepository.findByIdProgramaAndIdAsignatura(idPrograma, idAsignatura)
                .orElse(new PlanEstudio(idPrograma, idAsignatura, semestre));
        fila.setSemestre(semestre);
        if (plan != null) {
            fila.setPlan(nombrePlan(plan));
        }
        if (estadoPlan != null) {
            fila.setEstadoPlan(estadoPlan);
        }
        planRepository.save(fila);

        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.GUARDAR_PLAN_ESTUDIO,
                nombre(programa) + ": " + asignatura.getCodigo() + " en semestre " + semestre
                        + (fila.getPlan() == null ? "" : " (" + fila.getPlan() + ")")
                        + " - " + fila.getEstadoPlan().name().toLowerCase(Locale.ROOT));
        return aDTO(fila, asignatura);
    }

    /** "Plan 2026", "2020-1"... (letras, numeros, espacios y . - / ( )), o null si viene vacio. */
    static String nombrePlan(String plan) {
        String limpio = plan.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > PLAN_MAXIMO) {
            throw new BusinessException("El nombre o periodo del plan admite máximo " + PLAN_MAXIMO + " caracteres.");
        }
        if (!limpio.matches("[\\p{L}\\p{N} .\\-/()]+")) {
            throw new BusinessException("El nombre o periodo del plan solo admite letras, números, espacios y . - / ( ).");
        }
        return limpio;
    }

    static EstadoPlanEstudio estadoPlan(String estado) {
        try {
            return EstadoPlanEstudio.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("El estado del plan debe ser VIGENTE, ANTIGUO o DESACTUALIZADO.");
        }
    }

    /** Quita la asignatura del plan del programa (la asignatura NO se borra). */
    public void quitar(Integer idPrograma, Integer idAsignatura) {
        Programa programa = programa(idPrograma);
        PlanEstudio fila = planRepository.findByIdProgramaAndIdAsignatura(idPrograma, idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException("Esa asignatura no está en el plan del programa."));
        planRepository.delete(fila);
        String codigo = asignaturaRepository.findById(idAsignatura).map(Asignatura::getCodigo).orElse("#" + idAsignatura);
        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.QUITAR_PLAN_ESTUDIO,
                nombre(programa) + ": " + codigo);
    }

    public static void validarSemestre(Integer semestre) {
        if (semestre == null || semestre < 1 || semestre > SEMESTRE_MAXIMO) {
            throw new BusinessException("El semestre debe ser un número entre 1 y " + SEMESTRE_MAXIMO + ".");
        }
    }

    private Programa programa(Integer idPrograma) {
        return programaRepository.findById(idPrograma)
                .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado con id " + idPrograma));
    }

    private static String nombre(Programa p) {
        return (p.getCodigo() == null ? "" : p.getCodigo() + " - ") + p.getNombre();
    }

    private static PlanEstudioDTO aDTO(PlanEstudio fila, Asignatura a) {
        String[] codigos = AsignaturaService.separar(a.getCodigo());
        return new PlanEstudioDTO(fila.getIdPrograma(), a.getId(), a.getCodigo(), codigos[0], codigos[1],
                a.getNombre(), fila.getSemestre(), fila.getPlan(),
                fila.getEstadoPlan() == null ? EstadoPlanEstudio.VIGENTE.name() : fila.getEstadoPlan().name());
    }
}
