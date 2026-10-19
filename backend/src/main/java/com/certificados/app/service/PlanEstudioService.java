package com.certificados.app.service;

import com.certificados.app.dto.PlanDTO;
import com.certificados.app.dto.PlanEstudioDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.EstadoPlanEstudio;
import com.certificados.app.model.Plan;
import com.certificados.app.model.PlanEstudio;
import com.certificados.app.model.Programa;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.PlanEstudioRepository;
import com.certificados.app.repository.PlanRepository;
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
 * PLANES DE ESTUDIO DE CADA PROGRAMA (US-09).
 *
 * Un programa puede tener varios planes (versiones del mismo plan), cada uno
 * con nombre editable (ej. "Plan 2026") y estado (vigente, antiguo o
 * desactualizado). Cada plan guarda en que semestre va cada asignatura.
 *
 * Lo usa la solicitud de certificado para pedir uno o varios semestres de
 * un plan, o el plan completo, y el certificado para agrupar las
 * asignaturas por semestre.
 *
 * SE PUEDE MODIFICAR: SEMESTRE_MAXIMO (si algun programa tiene mas semestres;
 * cambiar tambien el CHECK de la tabla en la base de datos) y NOMBRE_MAXIMO
 * (cambiar tambien la columna t_nombre de la tabla planes).
 */
@Service
@Transactional
public class PlanEstudioService {

    /** Semestres posibles: 1 .. SEMESTRE_MAXIMO. */
    public static final int SEMESTRE_MAXIMO = 20;

    /** Largo maximo del nombre del plan (columna t_nombre). */
    public static final int NOMBRE_MAXIMO = 100;

    private final PlanRepository planRepository;
    private final PlanEstudioRepository filaRepository;
    private final ProgramaRepository programaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final ActividadService actividadService;

    public PlanEstudioService(PlanRepository planRepository,
                              PlanEstudioRepository filaRepository,
                              ProgramaRepository programaRepository,
                              AsignaturaRepository asignaturaRepository,
                              ActividadService actividadService) {
        this.planRepository = planRepository;
        this.filaRepository = filaRepository;
        this.programaRepository = programaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.actividadService = actividadService;
    }

    /* =========================== PLANES =========================== */

    /** Todos los planes (de todos los programas): primero los vigentes, luego por nombre. */
    @Transactional(readOnly = true)
    public List<PlanDTO> listarTodos() {
        return planRepository.findAll().stream()
                .sorted(Comparator.comparing((Plan p) -> p.getEstado().ordinal())
                        .thenComparing(Plan::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::aDTO)
                .toList();
    }

    /** Planes del programa: primero los vigentes, luego por nombre. */
    @Transactional(readOnly = true)
    public List<PlanDTO> listarPlanes(Integer idPrograma) {
        programa(idPrograma);
        return planRepository.findByIdPrograma(idPrograma).stream()
                .sorted(Comparator.comparing((Plan p) -> p.getEstado().ordinal())
                        .thenComparing(Plan::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::aDTO)
                .toList();
    }

    public PlanDTO crearPlan(Integer idPrograma, String nombre, String estado) {
        Programa programa = programa(idPrograma);
        String limpio = nombrePlan(nombre);
        if (planRepository.existsByIdProgramaAndNombreIgnoreCase(idPrograma, limpio)) {
            throw new BusinessException("El programa ya tiene un plan llamado «" + limpio + "».");
        }
        EstadoPlanEstudio estadoPlan = estado == null || estado.isBlank()
                ? EstadoPlanEstudio.VIGENTE : estadoPlan(estado);
        Plan plan = planRepository.save(new Plan(idPrograma, limpio, estadoPlan));
        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.CREAR_PLAN,
                nombre(programa) + ": " + limpio + " (" + texto(estadoPlan) + ")");
        return aDTO(plan);
    }

    /** Cambia el nombre y/o el estado del plan (null = no cambiarlo). */
    public PlanDTO actualizarPlan(Integer idPlan, String nombre, String estado) {
        Plan plan = plan(idPlan);
        if (nombre != null) {
            String limpio = nombrePlan(nombre);
            if (planRepository.existsByIdProgramaAndNombreIgnoreCaseAndIdNot(plan.getIdPrograma(), limpio, idPlan)) {
                throw new BusinessException("El programa ya tiene otro plan llamado «" + limpio + "».");
            }
            plan.setNombre(limpio);
        }
        if (estado != null) {
            plan.setEstado(estadoPlan(estado));
        }
        planRepository.save(plan);
        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.ACTUALIZAR_PLAN,
                nombre(programa(plan.getIdPrograma())) + ": " + plan.getNombre() + " (" + texto(plan.getEstado()) + ")");
        return aDTO(plan);
    }

    /**
     * Elimina el plan y sus asignaturas por semestre (las asignaturas NO se
     * borran del sistema). Las solicitudes que lo usaban quedan sin plan.
     */
    public void eliminarPlan(Integer idPlan) {
        Plan plan = plan(idPlan);
        String detalle = nombre(programa(plan.getIdPrograma())) + ": " + plan.getNombre();
        filaRepository.deleteAll(filaRepository.findByIdPlanOrderBySemestreAscIdAsignaturaAsc(idPlan));
        planRepository.delete(plan);
        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.ELIMINAR_PLAN, detalle);
    }

    /* ==================== ASIGNATURAS DEL PLAN ==================== */

    /** Asignaturas del plan, ordenadas por semestre y codigo. */
    @Transactional(readOnly = true)
    public List<PlanEstudioDTO> listar(Integer idPlan) {
        plan(idPlan);
        List<PlanEstudio> filas = filaRepository.findByIdPlanOrderBySemestreAscIdAsignaturaAsc(idPlan);
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

    /** Agrega la asignatura al plan, o le cambia el semestre si ya estaba. */
    public PlanEstudioDTO guardar(Integer idPlan, Integer idAsignatura, Integer semestre) {
        Plan plan = plan(idPlan);
        Asignatura asignatura = asignaturaRepository.findById(idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + idAsignatura));
        validarSemestre(semestre);

        PlanEstudio fila = filaRepository.findByIdPlanAndIdAsignatura(idPlan, idAsignatura)
                .orElse(new PlanEstudio(idPlan, idAsignatura, semestre));
        fila.setSemestre(semestre);
        filaRepository.save(fila);

        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.GUARDAR_PLAN_ESTUDIO,
                plan.getNombre() + ": " + asignatura.getCodigo() + " en semestre " + semestre);
        return aDTO(fila, asignatura);
    }

    /** Quita la asignatura del plan (la asignatura NO se borra). */
    public void quitar(Integer idPlan, Integer idAsignatura) {
        Plan plan = plan(idPlan);
        PlanEstudio fila = filaRepository.findByIdPlanAndIdAsignatura(idPlan, idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException("Esa asignatura no está en el plan."));
        filaRepository.delete(fila);
        String codigo = asignaturaRepository.findById(idAsignatura).map(Asignatura::getCodigo).orElse("#" + idAsignatura);
        actividadService.registrar(ActividadService.TABLA_PLANES, ActividadService.QUITAR_PLAN_ESTUDIO,
                plan.getNombre() + ": " + codigo);
    }

    /* ======================== SOLICITUDES ========================= */

    /**
     * Plan que usara una solicitud por semestres o por programa completo.
     * Si se eligio uno, debe ser del programa; si no, se usa el unico plan
     * vigente del programa.
     */
    @Transactional(readOnly = true)
    public Plan planDeSolicitud(Integer idPrograma, Integer idPlan) {
        if (idPlan != null) {
            Plan plan = plan(idPlan);
            if (!plan.getIdPrograma().equals(idPrograma)) {
                throw new BusinessException("Ese plan de estudios no pertenece al programa elegido.");
            }
            return plan;
        }
        List<Plan> vigentes = planRepository.findByIdProgramaAndEstado(idPrograma, EstadoPlanEstudio.VIGENTE);
        if (vigentes.size() == 1) {
            return vigentes.get(0);
        }
        throw new BusinessException(vigentes.isEmpty()
                ? "El programa no tiene un plan de estudios vigente. Elige el plan en la solicitud."
                : "El programa tiene varios planes vigentes. Elige el plan en la solicitud.");
    }

    @Transactional(readOnly = true)
    public String nombrePlan(Integer idPlan) {
        return idPlan == null ? null : planRepository.findById(idPlan).map(Plan::getNombre).orElse(null);
    }

    /** Ids de las asignaturas de los semestres indicados (todos si semestres es null). */
    @Transactional(readOnly = true)
    public List<Integer> asignaturasDe(Integer idPlan, Collection<Integer> semestres) {
        List<PlanEstudio> filas = semestres == null
                ? filaRepository.findByIdPlanOrderBySemestreAscIdAsignaturaAsc(idPlan)
                : filaRepository.findByIdPlanAndSemestreIn(idPlan, semestres);
        return filas.stream().map(PlanEstudio::getIdAsignatura).toList();
    }

    /** Semestre de cada asignatura en el plan (para agrupar el certificado). */
    @Transactional(readOnly = true)
    public Map<Integer, Integer> semestresPorAsignatura(Integer idPlan) {
        if (idPlan == null) {
            return Map.of();
        }
        return filaRepository.findByIdPlanOrderBySemestreAscIdAsignaturaAsc(idPlan).stream()
                .collect(Collectors.toMap(PlanEstudio::getIdAsignatura, PlanEstudio::getSemestre, (a, b) -> a));
    }

    /* ======================== VALIDACIONES ======================== */

    public static void validarSemestre(Integer semestre) {
        if (semestre == null || semestre < 1 || semestre > SEMESTRE_MAXIMO) {
            throw new BusinessException("El semestre debe ser un número entre 1 y " + SEMESTRE_MAXIMO + ".");
        }
    }

    /** "Plan 2026", "Plan de estudios (versión 3)"... */
    static String nombrePlan(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new BusinessException("Escribe el nombre del plan de estudios.");
        }
        if (limpio.length() > NOMBRE_MAXIMO) {
            throw new BusinessException("El nombre del plan admite máximo " + NOMBRE_MAXIMO + " caracteres.");
        }
        if (!limpio.matches("[\\p{L}\\p{N} .,:\\-/()]+")) {
            throw new BusinessException("El nombre del plan solo admite letras, números, espacios y . , : - / ( ).");
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

    private Plan plan(Integer idPlan) {
        return planRepository.findById(idPlan)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de estudios no encontrado con id " + idPlan));
    }

    private Programa programa(Integer idPrograma) {
        return programaRepository.findById(idPrograma)
                .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado con id " + idPrograma));
    }

    private static String nombre(Programa p) {
        return (p.getCodigo() == null ? "" : p.getCodigo() + " - ") + p.getNombre();
    }

    private static String texto(EstadoPlanEstudio estado) {
        return estado.name().toLowerCase(Locale.ROOT);
    }

    private PlanDTO aDTO(Plan p) {
        String programa = programaRepository.findById(p.getIdPrograma()).map(PlanEstudioService::nombre).orElse("");
        return new PlanDTO(p.getId(), p.getIdPrograma(), programa, p.getNombre(), p.getEstado().name(),
                filaRepository.countByIdPlan(p.getId()));
    }

    private static PlanEstudioDTO aDTO(PlanEstudio fila, Asignatura a) {
        String[] codigos = AsignaturaService.separar(a.getCodigo());
        return new PlanEstudioDTO(fila.getIdPlan(), a.getId(), a.getCodigo(), codigos[0], codigos[1],
                a.getNombre(), fila.getSemestre());
    }
}
