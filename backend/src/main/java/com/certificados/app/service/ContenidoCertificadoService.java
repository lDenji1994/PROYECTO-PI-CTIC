package com.certificados.app.service;

import com.certificados.app.dto.AsignaturaCertificadoDTO;
import com.certificados.app.dto.VersionDatosDTO;
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
 * Que version se elige (en este orden):
 *   1. una que tenga los datos del curso completos,
 *   2. con formato VIGENTE antes que uno desactualizado,
 *   3. del periodo mas reciente,
 *   4. la ultima cargada.
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

    public ContenidoCertificadoService(
            SolicitudCertificadoRepository solicitudRepository,
            DetalleSolicitudCertificadoRepository detalleRepository,
            AsignaturaRepository asignaturaRepository,
            DocumentoAcademicoRepository documentoRepository,
            TipoDocumentoAcademicoRepository tipoRepository,
            VersionDocumentoRepository versionRepository,
            ContenidoRepository contenidoRepository,
            FormatoService formatoService) {
        this.solicitudRepository = solicitudRepository;
        this.detalleRepository = detalleRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.documentoRepository = documentoRepository;
        this.tipoRepository = tipoRepository;
        this.versionRepository = versionRepository;
        this.contenidoRepository = contenidoRepository;
        this.formatoService = formatoService;
    }

    /** Contenido del certificado, asignatura por asignatura (ordenadas por codigo). */
    public List<AsignaturaCertificadoDTO> obtener(Integer idSolicitud) {
        if (!solicitudRepository.existsById(idSolicitud)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con id " + idSolicitud);
        }

        Map<Integer, TipoDocumentoAcademico> tipos = tipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoDocumentoAcademico::getId, Function.identity()));

        List<AsignaturaCertificadoDTO> resultado = new ArrayList<>();
        for (DetalleSolicitudCertificado detalle : detalleRepository.findByIdSolicitudCertificado(idSolicitud)) {
            asignaturaRepository.findById(detalle.getIdAsignatura())
                    .ifPresent(asignatura -> resultado.add(armar(asignatura, tipos)));
        }
        resultado.sort(Comparator.comparing(AsignaturaCertificadoDTO::codigo, String.CASE_INSENSITIVE_ORDER));
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

    private AsignaturaCertificadoDTO armar(Asignatura asignatura, Map<Integer, TipoDocumentoAcademico> tipos) {
        String[] codigos = AsignaturaService.separar(asignatura.getCodigo());

        Map<Integer, DocumentoAcademico> documentos = documentoRepository
                .findByIdAsignatura(asignatura.getId()).stream()
                .collect(Collectors.toMap(DocumentoAcademico::getId, Function.identity()));

        List<VersionDatosDTO> versiones = documentos.isEmpty()
                ? List.of()
                : versionRepository.listarDatosPorDocumentos(documentos.keySet());

        VersionDatosDTO elegida = versiones.stream()
                .min(orden(documentos))
                .orElse(null);

        if (elegida == null) {
            return new AsignaturaCertificadoDTO(asignatura.getId(), codigos[0], codigos[1],
                    asignatura.getCodigo(), asignatura.getNombre(), null, null, null, false, null,
                    null, null, null, null, null, null, List.of(), false,
                    List.of("sin documento cargado"));
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
                elegida.descripcion(), contenidos, faltantes.isEmpty(), faltantes);
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
