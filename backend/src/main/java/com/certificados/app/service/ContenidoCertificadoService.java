package com.certificados.app.service;

import com.certificados.app.dto.AsignaturaCertificadoDTO;
import com.certificados.app.dto.DetalleCompletoDTO;
import com.certificados.app.dto.VersionTextosDTO;
import com.certificados.app.model.AlcanceSolicitud;
import com.certificados.app.model.PlanEstudio;
import com.certificados.app.repository.PlanEstudioRepository;
import com.certificados.app.dto.VersionDatosDTO;
import com.certificados.app.dto.VersionOpcionDTO;
import com.certificados.app.dto.VersionResumenDTO;
import com.certificados.app.model.EstadoSolicitudCertificado;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.Contenido;
import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.ContenidoRepository;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.TipoDocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ARMA EL CONTENIDO DEL "CERTIFICADO DE CONTENIDOS RESUMIDOS".
 *
 * Para cada asignatura de la solicitud busca, entre sus documentos
 * cargados, la version de la que se tomara la informacion y devuelve:
 * creditos, horas, descripcion del curso y contenido (temas).
 *
 * Que version se usa:
 *   - Si la auxiliar escogio una version para esa asignatura en la solicitud
 *     (elegirVersion), se usa esa.
 *   - Si no, la elige el sistema en este orden:
 *       1. una que tenga los datos del curso completos,
 *       2. con formato VIGENTE antes que uno desactualizado,
 *       3. del periodo mas reciente,
 *       4. la ultima cargada.
 *
 * El certificado solo se genera si TODAS las asignaturas estan completas
 * (validarCompleto). Asi no sale un certificado con huecos.
 *
 * SE PUEDE MODIFICAR: la regla de seleccion (ORDEN) y lo que se considera
 * "completo" (metodo faltantes).
 */
@Service
// noRollbackFor: este servicio solo LEE. Si avisa "falta informacion" dentro
// de la transaccion de quien lo llama, no debe marcarla para deshacer
// (si lo hiciera, el mensaje se perderia y saldria un error 500).
@Transactional(readOnly = true,
        noRollbackFor = {BusinessException.class, ResourceNotFoundException.class})
public class ContenidoCertificadoService {

    private final SolicitudCertificadoRepository solicitudRepository;
    private final DetalleSolicitudCertificadoRepository detalleRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final DocumentoAcademicoRepository documentoRepository;
    private final TipoDocumentoAcademicoRepository tipoRepository;
    private final VersionDocumentoRepository versionRepository;
    private final ContenidoRepository contenidoRepository;
    private final FormatoService formatoService;
    private final ActividadService actividadService;
    private final PlanEstudioRepository planRepository;

    public ContenidoCertificadoService(
            SolicitudCertificadoRepository solicitudRepository,
            DetalleSolicitudCertificadoRepository detalleRepository,
            AsignaturaRepository asignaturaRepository,
            DocumentoAcademicoRepository documentoRepository,
            TipoDocumentoAcademicoRepository tipoRepository,
            VersionDocumentoRepository versionRepository,
            ContenidoRepository contenidoRepository,
            FormatoService formatoService,
            ActividadService actividadService,
            PlanEstudioRepository planRepository) {
        this.actividadService = actividadService;
        this.planRepository = planRepository;
        this.solicitudRepository = solicitudRepository;
        this.detalleRepository = detalleRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.documentoRepository = documentoRepository;
        this.tipoRepository = tipoRepository;
        this.versionRepository = versionRepository;
        this.contenidoRepository = contenidoRepository;
        this.formatoService = formatoService;
    }

    /**
     * Contenido del certificado, asignatura por asignatura.
     * Orden: por codigo; si la solicitud es por semestres o por programa,
     * primero por semestre (asi el certificado las agrupa por semestre).
     */
    public List<AsignaturaCertificadoDTO> obtener(Integer idSolicitud) {
        SolicitudCertificado solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id " + idSolicitud));

        // Semestre de cada asignatura en el plan de estudios de la solicitud (si tiene)
        Map<Integer, Integer> semestres = solicitud.getIdPlan() == null
                ? Map.of()
                : planRepository.findByIdPlanOrderBySemestreAscIdAsignaturaAsc(solicitud.getIdPlan()).stream()
                        .collect(Collectors.toMap(PlanEstudio::getIdAsignatura, PlanEstudio::getSemestre, (a, b) -> a));
        boolean porSemestre = solicitud.getAlcance() != null && solicitud.getAlcance() != AlcanceSolicitud.ASIGNATURAS;

        Map<Integer, TipoDocumentoAcademico> tipos = tipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoDocumentoAcademico::getId, Function.identity()));

        List<AsignaturaCertificadoDTO> resultado = new ArrayList<>();
        for (DetalleSolicitudCertificado detalle : detalleRepository.findByIdSolicitudCertificado(idSolicitud)) {
            asignaturaRepository.findById(detalle.getIdAsignatura())
                    .ifPresent(asignatura -> resultado.add(
                            armar(asignatura, tipos, detalle.getIdVersionDocumento(),
                                    porSemestre ? semestres.get(asignatura.getId()) : null)));
        }
        resultado.sort(Comparator
                .comparing(AsignaturaCertificadoDTO::semestre, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AsignaturaCertificadoDTO::codigo, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    /**
     * Comprueba que la solicitud tenga asignaturas y que todas tengan la
     * informacion necesaria. Si falta algo lanza BusinessException con el
     * detalle, para que la auxiliar sepa exactamente que completar.
     */
    public List<AsignaturaCertificadoDTO> validarCompleto(Integer idSolicitud) {
        List<AsignaturaCertificadoDTO> asignaturas = obtener(idSolicitud);
        if (asignaturas.isEmpty()) {
            throw new BusinessException("La solicitud no tiene asignaturas. Agrega al menos una antes de generar el certificado.");
        }
        String pendientes = asignaturas.stream()
                .filter(a -> !a.completo())
                .map(a -> a.codigo() + " (" + String.join(", ", a.faltantes()) + ")")
                .collect(Collectors.joining("; "));
        if (!pendientes.isEmpty()) {
            throw new BusinessException("Falta información para generar el certificado: " + pendientes
                    + ". Complétala en Carga de información.");
        }
        return asignaturas;
    }

    /* ------------------------------------------------------------------ */

    /**
     * La auxiliar escoge que version del documento usar para una asignatura
     * de la solicitud (por ejemplo, la del periodo en que el estudiante la
     * curso). idVersion = null vuelve a la eleccion automatica.
     * Solo mientras la solicitud no este cerrada (REALIZADO o ERROR) ni
     * generandose (PROCESANDO).
     */
    @Transactional
    public void elegirVersion(Integer idSolicitud, Integer idAsignatura, Integer idVersion) {
        SolicitudCertificado solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id " + idSolicitud));
        EstadoSolicitudCertificado estado = solicitud.getEstado();
        if (estado != EstadoSolicitudCertificado.PENDIENTE
                && estado != EstadoSolicitudCertificado.ESPERANDO_DOCUMENTOS) {
            throw new BusinessException("La solicitud #" + idSolicitud + " ya no se puede modificar (estado "
                    + estado + ").");
        }

        DetalleSolicitudCertificado detalle = detalleRepository.findByIdSolicitudCertificado(idSolicitud).stream()
                .filter(d -> d.getIdAsignatura().equals(idAsignatura))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Esa asignatura no está en la solicitud #" + idSolicitud + "."));
        Asignatura asignatura = asignaturaRepository.findById(idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + idAsignatura));

        String detalleBitacora;
        if (idVersion == null) {
            detalleBitacora = "automática";
        } else {
            // La version debe ser de un documento de ESA asignatura
            VersionResumenDTO version = versionRepository.buscarResumen(idVersion)
                    .orElseThrow(() -> new ResourceNotFoundException("La versión de documento ya no existe."));
            boolean esDeLaAsignatura = documentoRepository.findById(version.idDocumentoAcademico())
                    .map(d -> idAsignatura.equals(d.getIdAsignatura()))
                    .orElse(false);
            if (!esDeLaAsignatura) {
                throw new BusinessException("Esa versión no pertenece a la asignatura " + asignatura.getCodigo() + ".");
            }
            detalleBitacora = version.nombreArchivo()
                    + (version.periodo() == null ? "" : " (periodo " + version.periodo() + ")");
        }

        detalle.setIdVersionDocumento(idVersion);
        detalleRepository.save(detalle);
        actividadService.registrar(ActividadService.TABLA_DETALLE_SOLICITUD, ActividadService.ELEGIR_VERSION_SOLICITUD,
                "Solicitud #" + idSolicitud + ", " + asignatura.getCodigo() + ": " + detalleBitacora);
    }

    private AsignaturaCertificadoDTO armar(Asignatura asignatura, Map<Integer, TipoDocumentoAcademico> tipos,
                                           Integer idVersionElegida, Integer semestre) {
        String[] codigos = AsignaturaService.separar(asignatura.getCodigo());

        Map<Integer, DocumentoAcademico> documentos = documentoRepository
                .findByIdAsignatura(asignatura.getId()).stream()
                .collect(Collectors.toMap(DocumentoAcademico::getId, Function.identity()));

        List<VersionDatosDTO> versiones = documentos.isEmpty()
                ? List.of()
                : versionRepository.listarDatosPorDocumentos(documentos.keySet());

        // La que escogio la auxiliar (si sigue existiendo) o la automatica
        VersionDatosDTO manual = idVersionElegida == null ? null : versiones.stream()
                .filter(v -> v.id().equals(idVersionElegida))
                .findFirst()
                .orElse(null);
        VersionDatosDTO elegida = manual != null ? manual : versiones.stream()
                .min(orden(documentos))
                .orElse(null);

        List<VersionOpcionDTO> opciones = opciones(versiones, documentos, tipos);

        if (elegida == null) {
            return new AsignaturaCertificadoDTO(asignatura.getId(), codigos[0], codigos[1],
                    asignatura.getCodigo(), asignatura.getNombre(), null, null, null, false, null,
                    null, null, null, null, null, null, List.of(), false,
                    List.of("sin documento cargado"), false, opciones, null, semestre);
        }

        DocumentoAcademico documento = documentos.get(elegida.idDocumentoAcademico());
        TipoDocumentoAcademico tipo = tipos.get(documento.getIdTipoDocumentoAcademico());
        List<String> contenidos = contenidoRepository
                .findByIdVersionDocumentoOrderByOrdenAscIdAsc(elegida.id()).stream()
                .map(Contenido::getDescripcion)
                .filter(c -> c != null && !c.isBlank())
                .toList();
        List<String> faltantes = faltantes(elegida, contenidos);

        return new AsignaturaCertificadoDTO(
                asignatura.getId(), codigos[0], codigos[1], asignatura.getCodigo(), asignatura.getNombre(),
                elegida.id(),
                tipo == null ? null : tipo.getNombre(),
                documento.getCodigoFormato() + " v" + documento.getVersionFormato(),
                formatoService.esVigente(documento.getCodigoFormato(), documento.getVersionFormato()),
                elegida.periodo(),
                elegida.creditos(), elegida.horasTeoricas(), elegida.horasPracticas(),
                elegida.horasLaboratorio(), elegida.horasIndependientes(),
                elegida.descripcion(), contenidos, faltantes.isEmpty(), faltantes,
                manual != null, opciones, elegida.resumen(), semestre);
    }

    /**
     * Todo lo registrado de una version para el certificado COMPLETO:
     * proposito, justificacion, requisitos, modalidades y el contenido tema
     * por tema. Lo que no este registrado llega vacio y el PDF no lo muestra.
     */
    // noRollbackFor RuntimeException: se llama durante la generacion del PDF;
    // si fallara una lectura, la solicitud debe poder quedar en ERROR.
    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public DetalleCompletoDTO detalleCompleto(Integer idVersion) {
        VersionTextosDTO textos = versionRepository.buscarTextos(idVersion)
                .orElse(new VersionTextosDTO(idVersion, null, null, null, null));

        List<DetalleCompletoDTO.TemaDetalle> contenidos = contenidoRepository
                .findByIdVersionDocumentoOrderByOrdenAscIdAsc(idVersion).stream()
                .filter(c -> tieneTexto(c.getDescripcion()))
                .map(c -> new DetalleCompletoDTO.TemaDetalle(c.getSemana(), c.getUnidad(), c.getTema(), c.getDescripcion()))
                .toList();

        return new DetalleCompletoDTO(textos.proposito(), textos.justificacion(), textos.requisitos(),
                textos.modalidades(), contenidos);
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    /** Versiones que se pueden escoger, de la mas reciente a la mas antigua. */
    private List<VersionOpcionDTO> opciones(List<VersionDatosDTO> versiones,
                                            Map<Integer, DocumentoAcademico> documentos,
                                            Map<Integer, TipoDocumentoAcademico> tipos) {
        return versiones.stream()
                .sorted(Comparator
                        .comparing((VersionDatosDTO v) -> v.periodo() == null ? "" : v.periodo(), Comparator.reverseOrder())
                        .thenComparing(v -> v.fechaCarga() == null ? LocalDateTime.MIN : v.fechaCarga(), Comparator.reverseOrder())
                        .thenComparing(VersionDatosDTO::id, Comparator.reverseOrder()))
                .map(v -> {
                    DocumentoAcademico d = documentos.get(v.idDocumentoAcademico());
                    TipoDocumentoAcademico tipo = tipos.get(d.getIdTipoDocumentoAcademico());
                    return new VersionOpcionDTO(v.id(), v.periodo(),
                            tipo == null ? null : tipo.getNombre(),
                            d.getCodigoFormato() + " v" + d.getVersionFormato(),
                            formatoService.esVigente(d.getCodigoFormato(), d.getVersionFormato()),
                            v.fechaCarga(), tieneDatos(v));
                })
                .toList();
    }

    /** Menor = mejor candidata (ver el comentario de la clase). */
    private Comparator<VersionDatosDTO> orden(Map<Integer, DocumentoAcademico> documentos) {
        return Comparator
                .comparing((VersionDatosDTO v) -> tieneDatos(v) ? 0 : 1)
                .thenComparing(v -> {
                    DocumentoAcademico d = documentos.get(v.idDocumentoAcademico());
                    return formatoService.esVigente(d.getCodigoFormato(), d.getVersionFormato()) ? 0 : 1;
                })
                .thenComparing(v -> v.periodo() == null ? "" : v.periodo(), Comparator.reverseOrder())
                .thenComparing(v -> v.fechaCarga() == null ? LocalDateTime.MIN : v.fechaCarga(), Comparator.reverseOrder())
                .thenComparing(VersionDatosDTO::id, Comparator.reverseOrder());
    }

    private static boolean tieneDatos(VersionDatosDTO v) {
        return v.descripcion() != null && !v.descripcion().isBlank() && v.creditos() != null;
    }

    private static List<String> faltantes(VersionDatosDTO v, List<String> contenidos) {
        List<String> faltan = new ArrayList<>();
        if (v.descripcion() == null || v.descripcion().isBlank()) {
            faltan.add("sin descripción del curso");
        }
        if (contenidos.isEmpty()) {
            faltan.add("sin detalle de contenido");
        }
        if (v.creditos() == null) {
            faltan.add("sin créditos");
        }
        return faltan;
    }
}
