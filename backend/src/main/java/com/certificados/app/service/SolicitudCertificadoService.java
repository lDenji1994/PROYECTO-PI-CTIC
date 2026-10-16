package com.certificados.app.service;

import com.certificados.app.dto.SolicitudCertificadoDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.security.UsuarioDetallesService;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.AlcanceSolicitud;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.EstadoSolicitudCertificado;
import com.certificados.app.model.Log;
import com.certificados.app.model.PlantillaCertificado;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.TipoContenidoCertificado;
import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.LogRepository;
import com.certificados.app.repository.PlantillaCertificadoRepository;
import com.certificados.app.repository.ProgramaRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.TipoCertificadoRepository;
import com.certificados.app.repository.UsuarioRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class SolicitudCertificadoService {

    private final SolicitudCertificadoRepository repository;
    private final AsignaturaRepository asignaturaRepository;
    private final DetalleSolicitudCertificadoRepository detalleRepository;
    private final TipoCertificadoRepository tipoCertificadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlantillaCertificadoRepository plantillaCertificadoRepository;
    private final VersionPlantillaRepository versionPlantillaRepository;
    private final CertificadoGeneradorService certificadoGeneradorService;
    private final LogRepository logRepository;
    private final ActividadService actividadService;
    private final ContenidoCertificadoService contenidoCertificadoService;
    private final PlanEstudioService planEstudioService;
    private final ProgramaRepository programaRepository;

    /**
     * Caracteres permitidos en el nombre del estudiante: letras (con tildes),
     * espacios, punto, guion y apostrofe. Asi no se guarda codigo (HTML/JS).
     * SE PUEDE MODIFICAR: el largo maximo debe ser <= 150 (columna t_nombreEstudiante).
     */
    private static final String PATRON_NOMBRE = "^[\\p{L}][\\p{L} .'-]{1,149}$";

    public SolicitudCertificadoService(
            SolicitudCertificadoRepository repository,
            AsignaturaRepository asignaturaRepository,
            DetalleSolicitudCertificadoRepository detalleRepository,
            TipoCertificadoRepository tipoCertificadoRepository,
            UsuarioRepository usuarioRepository,
            PlantillaCertificadoRepository plantillaCertificadoRepository,
            VersionPlantillaRepository versionPlantillaRepository,
            CertificadoGeneradorService certificadoGeneradorService,
            LogRepository logRepository,
            ActividadService actividadService,
            ContenidoCertificadoService contenidoCertificadoService,
            PlanEstudioService planEstudioService,
            ProgramaRepository programaRepository) {

        this.repository = repository;
        this.asignaturaRepository = asignaturaRepository;
        this.detalleRepository = detalleRepository;
        this.tipoCertificadoRepository = tipoCertificadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.plantillaCertificadoRepository =
                plantillaCertificadoRepository;
        this.versionPlantillaRepository =
                versionPlantillaRepository;
        this.certificadoGeneradorService =
                certificadoGeneradorService;
        this.logRepository = logRepository;
        this.actividadService = actividadService;
        this.contenidoCertificadoService = contenidoCertificadoService;
        this.planEstudioService = planEstudioService;
        this.programaRepository = programaRepository;
    }

    public List<SolicitudCertificadoDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<SolicitudCertificadoDTO> listarPorEstado(
            EstadoSolicitudCertificado estado) {

        return repository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<SolicitudCertificadoDTO> listarPorEstudiante(
            Integer idEstudiante) {

        return repository.findByIdEstudiante(idEstudiante)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public SolicitudCertificadoDTO buscarPorId(Integer id) {
        return toDTO(obtener(id));
    }

    public SolicitudCertificadoDTO crear(
            SolicitudCertificadoDTO dto) {

        // El encargado es quien tiene la sesion iniciada. Solo el
        // administrador puede asignar la solicitud a otra persona.
        UsuarioDetallesService.actual().ifPresent(u -> {
            if (dto.getIdUsuarioEncargado() == null || !u.esAdministrador()) {
                dto.setIdUsuarioEncargado(u.getId());
            }
        });

        if (dto.getIdEstudiante() == null) {
            throw new IllegalArgumentException(
                    "El identificador del estudiante es obligatorio"
            );
        }

        if (dto.getIdTipoCertificado() == null) {
            throw new IllegalArgumentException(
                    "El tipo de certificado es obligatorio"
            );
        }

        if (dto.getIdUsuarioEncargado() == null) {
            throw new IllegalArgumentException(
                    "El usuario encargado es obligatorio"
            );
        }

        if (!tipoCertificadoRepository.existsById(
                dto.getIdTipoCertificado())) {

            throw new ResourceNotFoundException(
                    "Tipo de certificado no encontrado con id "
                            + dto.getIdTipoCertificado()
            );
        }

        if (!usuarioRepository.existsById(
                dto.getIdUsuarioEncargado())) {

            throw new ResourceNotFoundException(
                    "Usuario encargado no encontrado con id "
                            + dto.getIdUsuarioEncargado()
            );
        }

        String nombreEstudiante = normalizarNombre(dto.getNombreEstudiante());
        TipoContenidoCertificado tipoContenido = dto.getTipoContenido() == null
                ? TipoContenidoCertificado.RESUMIDO
                : dto.getTipoContenido();
        AlcanceSolicitud alcance = dto.getAlcance() == null
                ? AlcanceSolicitud.ASIGNATURAS
                : dto.getAlcance();

        // Semestres / programa completo: las asignaturas salen del plan de estudios
        List<Integer> asignaturasDelPlan = List.of();
        String semestresTexto = null;
        Integer idPrograma = null;
        if (alcance != AlcanceSolicitud.ASIGNATURAS) {
            idPrograma = dto.getIdPrograma();
            if (idPrograma == null || !programaRepository.existsById(idPrograma)) {
                throw new BusinessException("Seleccione el programa académico de la solicitud.");
            }
            TreeSet<Integer> semestres = null;
            if (alcance == AlcanceSolicitud.SEMESTRES) {
                if (dto.getSemestres() == null || dto.getSemestres().isEmpty()) {
                    throw new BusinessException("Seleccione al menos un semestre.");
                }
                semestres = new TreeSet<>();
                for (Integer semestre : dto.getSemestres()) {
                    PlanEstudioService.validarSemestre(semestre);
                    semestres.add(semestre);
                }
                semestresTexto = semestres.stream().map(String::valueOf).collect(Collectors.joining(","));
            }
            asignaturasDelPlan = planEstudioService.asignaturasDe(idPrograma, semestres);
            if (asignaturasDelPlan.isEmpty()) {
                throw new BusinessException(alcance == AlcanceSolicitud.SEMESTRES
                        ? "El plan de estudios del programa no tiene asignaturas en esos semestres. "
                          + "Regístralas en «Contenido de cursos» > Plan de estudios."
                        : "El programa no tiene plan de estudios registrado. "
                          + "Regístralo en «Contenido de cursos» > Plan de estudios.");
            }
        }

        SolicitudCertificado solicitud =
                new SolicitudCertificado();

        solicitud.setNombreEstudiante(nombreEstudiante);
        solicitud.setTipoContenido(tipoContenido);
        solicitud.setAlcance(alcance);
        solicitud.setIdPrograma(idPrograma);
        solicitud.setSemestres(semestresTexto);

        solicitud.setIdEstudiante(
                dto.getIdEstudiante()
        );

        solicitud.setIdTipoCertificado(
                dto.getIdTipoCertificado()
        );

        solicitud.setIdUsuarioEncargado(
                dto.getIdUsuarioEncargado()
        );

        solicitud.setEstado(
                EstadoSolicitudCertificado.PENDIENTE
        );

        solicitud.setFechaSolicitud(
                LocalDateTime.now()
        );

        SolicitudCertificado guardada = repository.save(solicitud);

        for (Integer idAsignatura : asignaturasDelPlan) {
            detalleRepository.save(new DetalleSolicitudCertificado(guardada.getId(), idAsignatura));
        }

        registrar(guardada, ActividadService.CREAR_SOLICITUD,
                " (" + textoAlcance(guardada) + ", " + tipoContenido.name().toLowerCase() + ")");

        return toDTO(guardada);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public SolicitudCertificadoDTO iniciarProcesamiento(
            Integer id) {

        SolicitudCertificado solicitud = obtener(id);

        validarTransicion(
                solicitud.getEstado(),
                EstadoSolicitudCertificado.PROCESANDO
        );

        VersionPlantilla version =
                obtenerVersionPlantillaDisponible(
                        solicitud.getIdTipoCertificado()
                );

        // Antes de cambiar el estado: si a alguna asignatura le falta
        // informacion (documento, descripcion, contenido, creditos) se avisa
        // y la solicitud queda como estaba, para completarla y reintentar.
        // Asi no termina en ERROR por un dato que solo faltaba registrar.
        contenidoCertificadoService.validarCompleto(solicitud.getId());

        solicitud.setEstado(
                EstadoSolicitudCertificado.PROCESANDO
        );

        solicitud.setFechaInicioProcesamiento(
                LocalDateTime.now()
        );

        repository.save(solicitud);

        Log log = new Log();

        log.setIp(actividadService.ipActual());
        log.setNombreTabla(ActividadService.TABLA_CERTIFICADOS);
        log.setNombreProceso("GENERAR_CERTIFICADO");

        // A nombre de quien pulsa "Generar PDF" (si no hay sesion, el encargado)
        log.setIdUsuario(
                UsuarioDetallesService.actual()
                        .map(u -> u.getId())
                        .orElse(solicitud.getIdUsuarioEncargado())
        );

        log.setFechaInicio(
                LocalDateTime.now()
        );

        log = logRepository.save(log);

        try {

            certificadoGeneradorService.generar(
                    solicitud.getId(),
                    version.getId()
            );

            log.setFechaFin(
                    LocalDateTime.now()
            );

            logRepository.save(log);

            solicitud.setEstado(
                    EstadoSolicitudCertificado.REALIZADO
            );

            solicitud.setFechaFinalizacion(
                    LocalDateTime.now()
            );

            return toDTO(
                    repository.save(solicitud)
            );

        } catch (RuntimeException e) {

            log.setFechaFin(
                    LocalDateTime.now()
            );

            logRepository.save(log);

            solicitud.setEstado(
                    EstadoSolicitudCertificado.ERROR
            );

            solicitud.setFechaFinalizacion(
                    LocalDateTime.now()
            );

            repository.save(solicitud);

            // Si el mensaje ya viene explicado (BusinessException) no se repite el prefijo
            throw new BusinessException(
                    e instanceof BusinessException
                            ? e.getMessage()
                            : "No fue posible generar el certificado: " + e.getMessage()
            );
        }
    }

    public SolicitudCertificadoDTO esperarDocumentos(
            Integer id) {

        SolicitudCertificado solicitud = obtener(id);

        validarTransicion(
                solicitud.getEstado(),
                EstadoSolicitudCertificado.ESPERANDO_DOCUMENTOS
        );

        solicitud.setEstado(
                EstadoSolicitudCertificado.ESPERANDO_DOCUMENTOS
        );

        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.SOLICITUD_ESPERANDO, "");

        return toDTO(guardada);
    }

    public SolicitudCertificadoDTO marcarRealizada(
            Integer id) {

        SolicitudCertificado solicitud = obtener(id);

        validarTransicion(
                solicitud.getEstado(),
                EstadoSolicitudCertificado.REALIZADO
        );

        solicitud.setEstado(
                EstadoSolicitudCertificado.REALIZADO
        );

        solicitud.setFechaFinalizacion(
                LocalDateTime.now()
        );

        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.SOLICITUD_REALIZADA, "");

        return toDTO(guardada);
    }

    /**
     * El certificado ya se le entrego al estudiante: REALIZADO -> ENTREGADO.
     * Asi se distingue lo que solo se genero de lo que ya se entrego.
     */
    public SolicitudCertificadoDTO marcarEntregada(Integer id) {
        SolicitudCertificado solicitud = obtener(id);
        validarTransicion(solicitud.getEstado(), EstadoSolicitudCertificado.ENTREGADO);
        solicitud.setEstado(EstadoSolicitudCertificado.ENTREGADO);
        solicitud.setFechaEntrega(LocalDateTime.now());
        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.SOLICITUD_ENTREGADA, "");
        return toDTO(guardada);
    }

    /** Por si se marco como entregada por error: ENTREGADO -> REALIZADO. */
    public SolicitudCertificadoDTO deshacerEntrega(Integer id) {
        SolicitudCertificado solicitud = obtener(id);
        validarTransicion(solicitud.getEstado(), EstadoSolicitudCertificado.REALIZADO);
        solicitud.setEstado(EstadoSolicitudCertificado.REALIZADO);
        solicitud.setFechaEntrega(null);
        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.SOLICITUD_ENTREGA_DESHECHA, "");
        return toDTO(guardada);
    }

    /**
     * Cambia el nombre del estudiante y/o el tipo de contenido (resumido o
     * completo) antes de generar el PDF. Los campos que lleguen en null no
     * se cambian. Solo mientras la solicitud este PENDIENTE o ESPERANDO_DOCUMENTOS.
     */
    public SolicitudCertificadoDTO actualizarDatos(Integer id, SolicitudCertificadoDTO dto) {
        SolicitudCertificado solicitud = obtener(id);
        if (solicitud.getEstado() != EstadoSolicitudCertificado.PENDIENTE
                && solicitud.getEstado() != EstadoSolicitudCertificado.ESPERANDO_DOCUMENTOS) {
            throw new BusinessException("La solicitud #" + id + " ya no se puede modificar (estado "
                    + solicitud.getEstado() + ").");
        }
        if (dto.getNombreEstudiante() != null) {
            solicitud.setNombreEstudiante(normalizarNombre(dto.getNombreEstudiante()));
        }
        if (dto.getTipoContenido() != null) {
            solicitud.setTipoContenido(dto.getTipoContenido());
        }
        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.ACTUALIZAR_SOLICITUD,
                " (" + guardada.getTipoContenido().name().toLowerCase() + ")");
        return toDTO(guardada);
    }

    /** Quita espacios de mas y valida el nombre del estudiante (obligatorio). */
    private static String normalizarNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new BusinessException("El nombre del estudiante es obligatorio.");
        }
        if (!limpio.matches(PATRON_NOMBRE)) {
            throw new BusinessException("El nombre del estudiante solo puede tener letras, espacios, "
                    + "punto, guion y apóstrofe (máximo 150 caracteres).");
        }
        return limpio;
    }

    /** "asignaturas", "semestres 1, 2 de ISIS" o "programa ISIS completo" (para la bitacora). */
    private String textoAlcance(SolicitudCertificado s) {
        String programa = s.getIdPrograma() == null ? "" : programaRepository.findById(s.getIdPrograma())
                .map(p -> p.getCodigo() == null ? p.getNombre() : p.getCodigo()).orElse("");
        return switch (s.getAlcance()) {
            case ASIGNATURAS -> "asignaturas";
            case SEMESTRES -> "semestres " + s.getSemestres().replace(",", ", ") + " de " + programa;
            case PROGRAMA -> "programa " + programa + " completo";
        };
    }

    public SolicitudCertificadoDTO marcarError(
            Integer id) {

        SolicitudCertificado solicitud = obtener(id);

        validarTransicion(
                solicitud.getEstado(),
                EstadoSolicitudCertificado.ERROR
        );

        solicitud.setEstado(
                EstadoSolicitudCertificado.ERROR
        );

        solicitud.setFechaFinalizacion(
                LocalDateTime.now()
        );

        SolicitudCertificado guardada = repository.save(solicitud);
        registrar(guardada, ActividadService.SOLICITUD_ERROR, "");

        return toDTO(guardada);
    }

    public void agregarAsignatura(
            Integer idSolicitud,
            Integer idAsignatura) {

        obtener(idSolicitud);

        asignaturaRepository.findById(idAsignatura)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asignatura no encontrada con id "
                                        + idAsignatura
                        ));

        boolean existe =
                detalleRepository
                        .existsByIdSolicitudCertificadoAndIdAsignatura(
                                idSolicitud,
                                idAsignatura
                        );

        if (existe) {
            throw new IllegalArgumentException(
                    "La asignatura ya está asociada a la solicitud"
            );
        }

        DetalleSolicitudCertificado detalle =
                new DetalleSolicitudCertificado(
                        idSolicitud,
                        idAsignatura
                );

        detalleRepository.save(detalle);

        SolicitudCertificado solicitud = obtener(idSolicitud);
        String asignatura = asignaturaRepository.findById(idAsignatura)
                .map(a -> a.getCodigo() + " - " + a.getNombre()).orElse("#" + idAsignatura);
        actividadService.registrar(ActividadService.TABLA_DETALLE_SOLICITUD,
                ActividadService.AGREGAR_ASIGNATURA_SOLICITUD,
                "Solicitud #" + idSolicitud + ": " + asignatura,
                solicitud.getIdUsuarioEncargado());
    }

    /** Bitacora de solicitudes (a nombre del usuario encargado). */
    private void registrar(SolicitudCertificado solicitud, String proceso, String extra) {
        actividadService.registrar(ActividadService.TABLA_SOLICITUDES, proceso,
                "Solicitud #" + solicitud.getId() + " - estudiante " + solicitud.getIdEstudiante()
                        + (solicitud.getNombreEstudiante() == null ? "" : " " + solicitud.getNombreEstudiante())
                        + extra,
                solicitud.getIdUsuarioEncargado());
    }

    public List<Asignatura> listarAsignaturas(
            Integer idSolicitud) {

        obtener(idSolicitud);

        return detalleRepository
                .findByIdSolicitudCertificado(idSolicitud)
                .stream()
                .map(detalle ->
                        asignaturaRepository
                                .findById(
                                        detalle.getIdAsignatura()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Asignatura asociada no encontrada"
                                        ))
                )
                .toList();
    }

    private VersionPlantilla obtenerVersionPlantillaDisponible(
            Integer idTipoCertificado) {

        List<PlantillaCertificado> plantillas =
                plantillaCertificadoRepository
                        .findByIdTipoCertificado(
                                idTipoCertificado
                        );

        if (plantillas.isEmpty()) {
            throw new BusinessException(
                    "No existe una plantilla para el tipo de "
                            + "certificado solicitado"
            );
        }

        for (PlantillaCertificado plantilla : plantillas) {

            List<VersionPlantilla> versiones =
                    versionPlantillaRepository
                            .findByIdPlantillaCertificado(
                                    plantilla.getId()
                            );

            if (!versiones.isEmpty()) {
                return versiones.get(0);
            }
        }

        throw new BusinessException(
                "La plantilla del tipo de certificado "
                        + "no tiene una versión disponible"
        );
    }

    private void validarTransicion(
            EstadoSolicitudCertificado estadoActual,
            EstadoSolicitudCertificado estadoNuevo) {

        boolean valida = switch (estadoActual) {

            case PENDIENTE ->
                    estadoNuevo == EstadoSolicitudCertificado.PROCESANDO
                            || estadoNuevo == EstadoSolicitudCertificado.ERROR;

            case PROCESANDO ->
                    estadoNuevo == EstadoSolicitudCertificado.ESPERANDO_DOCUMENTOS
                            || estadoNuevo == EstadoSolicitudCertificado.REALIZADO
                            || estadoNuevo == EstadoSolicitudCertificado.ERROR;

            case ESPERANDO_DOCUMENTOS ->
                    estadoNuevo == EstadoSolicitudCertificado.PROCESANDO
                            || estadoNuevo == EstadoSolicitudCertificado.ERROR;

            // El PDF ya esta generado: solo falta entregarlo al estudiante
            case REALIZADO ->
                    estadoNuevo == EstadoSolicitudCertificado.ENTREGADO;

            // Deshacer una entrega marcada por error
            case ENTREGADO ->
                    estadoNuevo == EstadoSolicitudCertificado.REALIZADO;

            case ERROR ->
                    false;
        };

        if (!valida) {
            throw new BusinessException(
                    "Transición de estado no permitida: "
                            + estadoActual
                            + " -> "
                            + estadoNuevo
            );
        }
    }

    private SolicitudCertificado obtener(Integer id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Solicitud no encontrada con id " + id
                        ));
    }

    private SolicitudCertificadoDTO toDTO(
            SolicitudCertificado solicitud) {

        SolicitudCertificadoDTO dto =
                new SolicitudCertificadoDTO();

        dto.setId(solicitud.getId());

        dto.setIdEstudiante(
                solicitud.getIdEstudiante()
        );

        dto.setIdTipoCertificado(
                solicitud.getIdTipoCertificado()
        );

        dto.setIdUsuarioEncargado(
                solicitud.getIdUsuarioEncargado()
        );

        dto.setEstado(
                solicitud.getEstado()
        );

        dto.setFechaSolicitud(
                solicitud.getFechaSolicitud()
        );

        dto.setFechaInicioProcesamiento(
                solicitud.getFechaInicioProcesamiento()
        );

        dto.setFechaFinalizacion(
                solicitud.getFechaFinalizacion()
        );

        dto.setNombreEstudiante(solicitud.getNombreEstudiante());
        dto.setFechaEntrega(solicitud.getFechaEntrega());
        dto.setTipoContenido(solicitud.getTipoContenido());
        dto.setAlcance(solicitud.getAlcance());
        dto.setIdPrograma(solicitud.getIdPrograma());
        dto.setSemestres(solicitud.getSemestres() == null || solicitud.getSemestres().isBlank()
                ? List.of()
                : java.util.Arrays.stream(solicitud.getSemestres().split(","))
                        .map(String::trim).filter(t -> !t.isEmpty()).map(Integer::valueOf).toList());

        return dto;
    }
}