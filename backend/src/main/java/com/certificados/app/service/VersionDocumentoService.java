package com.certificados.app.service;

import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.dto.DatosCursoDTO;
import com.certificados.app.dto.VersionDocumentoDTO;
import com.certificados.app.model.Contenido;
import com.certificados.app.repository.ContenidoRepository;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VersionDocumentoService {

    private final VersionDocumentoRepository repository;
    private final DocumentoAcademicoRepository documentoAcademicoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final FormatoService formatoService;
    private final ActividadService actividadService;
    private final ContenidoRepository contenidoRepository;
    private final ArchivoDocumentoValidador archivoValidador;

    public VersionDocumentoService(
            VersionDocumentoRepository repository,
            DocumentoAcademicoRepository documentoAcademicoRepository,
            AsignaturaRepository asignaturaRepository,
            FormatoService formatoService,
            ActividadService actividadService,
            ContenidoRepository contenidoRepository,
            ArchivoDocumentoValidador archivoValidador) {

        this.repository = repository;
        this.documentoAcademicoRepository = documentoAcademicoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.formatoService = formatoService;
        this.actividadService = actividadService;
        this.contenidoRepository = contenidoRepository;
        this.archivoValidador = archivoValidador;
    }

    public List<VersionDocumentoDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VersionDocumentoDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerEntidad(id));
    }

    public List<VersionDocumentoDTO> listarPorDocumento(
            Integer idDocumentoAcademico) {

        verificarDocumentoAcademico(idDocumentoAcademico);

        return repository.findByIdDocumentoAcademico(idDocumentoAcademico)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /** Carga solo el archivo (sin datos del curso). Se conserva por compatibilidad. */
    public VersionDocumento cargarArchivo(
            MultipartFile archivo,
            Integer idDocumentoAcademico,
            String periodo) throws IOException {

        return cargarArchivo(archivo, idDocumentoAcademico, periodo, null);
    }

    /**
     * Carga el archivo (PDF, Word o Excel) como una version nueva y, si se
     * envian, guarda los datos del curso registrados a mano.
     */
    public VersionDocumento cargarArchivo(
            MultipartFile archivo,
            Integer idDocumentoAcademico,
            String periodo,
            DatosCursoDTO datos) throws IOException {

        verificarDocumentoAcademico(idDocumentoAcademico);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo no llegó o está vacío. Vuelve a seleccionarlo e inténtalo de nuevo."
            );
        }

        String nombreArchivo = limpiarNombreArchivo(archivo.getOriginalFilename());

        if (nombreArchivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo debe tener un nombre válido"
            );
        }

        byte[] contenido = archivo.getBytes();

        // PDF, Word o Excel: se valida extension + firma real del contenido
        // (ver ArchivoDocumentoValidador). No se confia en el tipo que envia
        // el navegador.
        archivoValidador.validar(nombreArchivo, contenido);

        String periodoLimpio = (periodo == null || periodo.isBlank()) ? null : periodo.trim();
        if (periodoLimpio != null && !periodoLimpio.matches("^\\d{4}-\\d{1,2}$")) {
            throw new IllegalArgumentException(
                    "El periodo debe tener el formato AAAA-N (ej. 2026-2)"
            );
        }

        VersionDocumento version = new VersionDocumento();

        version.setIdDocumentoAcademico(idDocumentoAcademico);
        version.setPeriodo(periodoLimpio);
        version.setNombreArchivo(nombreArchivo);
        version.setArchivo(contenido);

        // Datos del curso escritos a mano por la auxiliar (si se enviaron)
        List<String> temas = datos == null ? List.of() : aplicarDatos(version, datos);

        VersionDocumento guardada = repository.save(version);

        guardarContenidos(guardada.getId(), temas);

        registrarCarga(idDocumentoAcademico, guardada);

        return guardada;
    }

    /* =====================================================================
       DATOS DEL CURSO REGISTRADOS A MANO
       ===================================================================== */

    /** Datos del curso de una version (para mostrarlos o corregirlos en el panel). */
    @Transactional(readOnly = true)
    public DatosCursoDTO obtenerDatos(Integer id) {
        return leerDatos(obtenerEntidad(id));
    }

    /** Corrige los datos del curso de una version ya cargada (no cambia el archivo). */
    public DatosCursoDTO actualizarDatos(Integer id, DatosCursoDTO datos) {
        VersionDocumento version = obtenerEntidad(id);
        List<String> temas = aplicarDatos(version, datos);
        repository.save(version);
        guardarContenidos(id, temas);

        actividadService.registrar(ActividadService.TABLA_VERSIONES,
                ActividadService.ACTUALIZAR_DATOS_CURSO, etiqueta(version));
        return leerDatos(version);
    }

    /**
     * Reemplaza el archivo de una version (por si se subio danado, incompleto
     * o equivocado). Los datos del curso se conservan.
     */
    public void reemplazarArchivo(Integer id, MultipartFile archivo) throws IOException {
        VersionDocumento version = obtenerEntidad(id);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo no llegó o está vacío. Vuelve a seleccionarlo e inténtalo de nuevo.");
        }
        String nombreArchivo = limpiarNombreArchivo(archivo.getOriginalFilename());
        if (nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El archivo debe tener un nombre válido");
        }
        byte[] contenido = archivo.getBytes();
        archivoValidador.validar(nombreArchivo, contenido);

        version.setNombreArchivo(nombreArchivo);
        version.setArchivo(contenido);
        repository.save(version);

        actividadService.registrar(ActividadService.TABLA_VERSIONES,
                ActividadService.REEMPLAZAR_ARCHIVO, etiqueta(version) + " -> " + nombreArchivo);
    }

    /**
     * Valida y copia los datos escritos a mano a la entidad.
     * Devuelve los temas del contenido (una linea = un tema) ya limpios.
     */
    private List<String> aplicarDatos(VersionDocumento version, DatosCursoDTO d) {
        String descripcion = texto(d.getDescripcion(), 6000, "La descripción del curso");
        if (descripcion == null) {
            throw new BusinessException("La descripción del curso es obligatoria");
        }

        List<String> temas = separarTemas(d.getDetalleContenido());
        if (temas.isEmpty()) {
            throw new BusinessException("El detalle de contenido del curso es obligatorio (un tema por línea)");
        }

        if (d.getCreditos() == null) {
            throw new BusinessException("El número de créditos es obligatorio");
        }
        numero(d.getCreditos(), "999.99", "Los créditos");
        if (d.getCreditos().signum() == 0) {
            throw new BusinessException("Los créditos deben ser mayores que cero");
        }

        BigDecimal[] horas = {d.getHorasTeoricas(), d.getHorasPracticas(),
                d.getHorasLaboratorio(), d.getHorasIndependientes()};
        boolean algunaHora = false;
        for (BigDecimal h : horas) {
            if (h != null) {
                numero(h, "9999.99", "Las horas");
                algunaHora = algunaHora || h.signum() > 0;
            }
        }
        if (!algunaHora) {
            throw new BusinessException("Registra al menos un valor de horas mayor que cero");
        }

        version.setDescripcion(descripcion);
        version.setCreditos(d.getCreditos());
        version.setHorasTeoricas(d.getHorasTeoricas());
        version.setHorasPracticas(d.getHorasPracticas());
        version.setHorasLaboratorio(d.getHorasLaboratorio());
        version.setHorasIndependientes(d.getHorasIndependientes());

        version.setEscuela(texto(d.getEscuela(), 150, "La escuela"));
        version.setFacultad(texto(d.getFacultad(), 150, "La facultad"));
        version.setClasificacionCINE(texto(d.getClasificacionCINE(), 100, "La clasificación CINE"));
        version.setNucleoBasicoConocimiento(texto(d.getNucleoBasicoConocimiento(), 2000, "El núcleo básico de conocimiento"));
        version.setCiclo(texto(d.getCiclo(), 100, "El ciclo"));
        version.setNivelFormacion(texto(d.getNivelFormacion(), 100, "El nivel de formación"));
        version.setRequisitos(texto(d.getRequisitos(), 4000, "Los requisitos"));
        version.setJustificacion(texto(d.getJustificacion(), 6000, "La justificación"));
        version.setProposito(texto(d.getProposito(), 6000, "El propósito"));
        version.setModoCalificacion(texto(d.getModoCalificacion(), 100, "El modo de calificación"));
        version.setModalidades(texto(d.getModalidades(), 2000, "Las modalidades"));
        version.setObservaciones(texto(d.getObservaciones(), 4000, "Las observaciones"));

        return temas;
    }

    private DatosCursoDTO leerDatos(VersionDocumento v) {
        DatosCursoDTO d = new DatosCursoDTO();
        d.setDescripcion(v.getDescripcion());
        d.setCreditos(v.getCreditos());
        d.setHorasTeoricas(v.getHorasTeoricas());
        d.setHorasPracticas(v.getHorasPracticas());
        d.setHorasLaboratorio(v.getHorasLaboratorio());
        d.setHorasIndependientes(v.getHorasIndependientes());
        d.setEscuela(v.getEscuela());
        d.setFacultad(v.getFacultad());
        d.setClasificacionCINE(v.getClasificacionCINE());
        d.setNucleoBasicoConocimiento(v.getNucleoBasicoConocimiento());
        d.setCiclo(v.getCiclo());
        d.setNivelFormacion(v.getNivelFormacion());
        d.setRequisitos(v.getRequisitos());
        d.setJustificacion(v.getJustificacion());
        d.setProposito(v.getProposito());
        d.setModoCalificacion(v.getModoCalificacion());
        d.setModalidades(v.getModalidades());
        d.setObservaciones(v.getObservaciones());
        d.setDetalleContenido(contenidoRepository
                .findByIdVersionDocumentoOrderByOrdenAscIdAsc(v.getId()).stream()
                .map(Contenido::getDescripcion)
                .collect(Collectors.joining("\n")));
        return d;
    }

    /** Reemplaza los contenidos de la version por los temas indicados (uno por fila). */
    private void guardarContenidos(Integer idVersion, List<String> temas) {
        if (temas.isEmpty()) {
            return;
        }
        contenidoRepository.deleteByIdVersionDocumento(idVersion);
        contenidoRepository.flush();
        int orden = 1;
        for (String tema : temas) {
            Contenido contenido = new Contenido();
            contenido.setIdVersionDocumento(idVersion);
            contenido.setOrden(orden++);
            contenido.setDescripcion(tema);
            contenidoRepository.save(contenido);
        }
    }

    /** Una linea no vacia = un tema. Maximo 300 temas de 1000 caracteres. */
    private static List<String> separarTemas(String detalle) {
        if (detalle == null) {
            return List.of();
        }
        List<String> temas = Arrays.stream(detalle.split("\\R"))
                .map(VersionDocumentoService::limpiarTexto)
                .filter(t -> !t.isEmpty())
                .toList();
        if (temas.size() > 300) {
            throw new BusinessException("El detalle de contenido no puede tener más de 300 líneas");
        }
        for (String t : temas) {
            if (t.length() > 1000) {
                throw new BusinessException("Cada línea del contenido puede tener máximo 1000 caracteres");
            }
        }
        return temas;
    }

    /** Texto opcional limpio; null si viene vacio. Valida la longitud maxima. */
    private static String texto(String valor, int maximo, String nombre) {
        if (valor == null) {
            return null;
        }
        // Se conservan los saltos de linea; se quitan otros caracteres de control
        String limpio = valor.replace("\r\n", "\n").replace('\r', '\n')
                .replaceAll("[\\p{Cntrl}&&[^\n\t]]", "").trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > maximo) {
            throw new BusinessException(nombre + " no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }

    private static String limpiarTexto(String linea) {
        return linea.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
    }

    private static void numero(BigDecimal valor, String maximo, String nombre) {
        if (valor.signum() < 0 || valor.compareTo(new BigDecimal(maximo)) > 0) {
            throw new BusinessException(nombre + " deben estar entre 0 y " + maximo);
        }
    }

    private String etiqueta(VersionDocumento version) {
        DocumentoAcademico documento = documentoAcademicoRepository
                .findById(version.getIdDocumentoAcademico()).orElse(null);
        if (documento == null) {
            return "Versión #" + version.getId();
        }
        return asignaturaRepository.findById(documento.getIdAsignatura())
                .map(a -> a.getCodigo() + " - " + a.getNombre())
                .orElse("Asignatura #" + documento.getIdAsignatura())
                + (version.getPeriodo() != null ? " (" + version.getPeriodo() + ")" : "");
    }

    /**
     * Bitacora: deja constancia de la carga y avisa si el formato del
     * documento esta desactualizado (se ve en amarillo en el Dashboard).
     */
    private void registrarCarga(Integer idDocumentoAcademico, VersionDocumento version) {
        DocumentoAcademico documento = documentoAcademicoRepository
                .findById(idDocumentoAcademico).orElse(null);
        if (documento == null) {
            return;
        }
        String asignatura = asignaturaRepository.findById(documento.getIdAsignatura())
                .map(a -> a.getCodigo() + " - " + a.getNombre())
                .orElse("Asignatura #" + documento.getIdAsignatura());
        boolean vigente = formatoService.esVigente(
                documento.getCodigoFormato(), documento.getVersionFormato());

        actividadService.registrar(
                ActividadService.TABLA_VERSIONES,
                vigente ? ActividadService.CARGAR_DOCUMENTO : ActividadService.CARGAR_DOCUMENTO_ANTIGUO,
                asignatura + " (" + documento.getCodigoFormato() + " v" + documento.getVersionFormato()
                        + (version.getPeriodo() != null ? ", " + version.getPeriodo() : "") + ")");
    }

    /**
     * Deja solo un nombre de archivo seguro: sin rutas (../), sin comillas
     * ni saltos de linea (evita inyeccion en la cabecera de descarga).
     */
    static String limpiarNombreArchivo(String original) {
        if (original == null) {
            return "";
        }
        String nombre = original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
        nombre = nombre.replaceAll("[^\\p{L}\\p{N} ._()\\-]", "_").trim();
        if (nombre.length() > 200) {
            nombre = nombre.substring(nombre.length() - 200);
        }
        return nombre;
    }

    public byte[] obtenerArchivo(Integer id) {
        return obtenerEntidad(id).getArchivo();
    }

    public String obtenerNombreArchivo(Integer id) {
        return obtenerEntidad(id).getNombreArchivo();
    }

    /** Tipo MIME segun la extension (PDF, Word o Excel). */
    public String tipoMime(String nombreArchivo) {
        return archivoValidador.tipoMime(nombreArchivo);
    }

    /** true si el archivo se puede mostrar dentro del navegador (solo PDF). */
    public boolean esPdf(String nombreArchivo) {
        return archivoValidador.esPdf(nombreArchivo);
    }

    private VersionDocumento obtenerEntidad(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Versión de documento no encontrada con id " + id
                        )
                );
    }

    private void verificarDocumentoAcademico(
            Integer idDocumentoAcademico) {

        if (idDocumentoAcademico == null) {
            throw new IllegalArgumentException(
                    "El documento académico es obligatorio"
            );
        }

        if (!documentoAcademicoRepository.existsById(
                idDocumentoAcademico)) {

            throw new IllegalArgumentException(
                    "No existe el documento académico con id "
                            + idDocumentoAcademico
            );
        }
    }

    private VersionDocumentoDTO convertirADTO(
            VersionDocumento version) {

        VersionDocumentoDTO dto = new VersionDocumentoDTO();

        dto.setId(version.getId());
        dto.setIdDocumentoAcademico(
                version.getIdDocumentoAcademico()
        );
        dto.setPeriodo(version.getPeriodo());
        dto.setNombreArchivo(version.getNombreArchivo());
        dto.setEscuela(version.getEscuela());
        dto.setFacultad(version.getFacultad());
        dto.setClasificacionCINE(
                version.getClasificacionCINE()
        );
        dto.setNucleoBasicoConocimiento(
                version.getNucleoBasicoConocimiento()
        );
        dto.setCiclo(version.getCiclo());
        dto.setNivelFormacion(version.getNivelFormacion());
        dto.setHorasTeoricas(version.getHorasTeoricas());
        dto.setHorasPracticas(version.getHorasPracticas());
        dto.setHorasLaboratorio(
                version.getHorasLaboratorio()
        );
        dto.setHorasIndependientes(
                version.getHorasIndependientes()
        );
        dto.setCreditos(version.getCreditos());
        dto.setRequisitos(version.getRequisitos());
        dto.setJustificacion(version.getJustificacion());
        dto.setDescripcion(version.getDescripcion());
        dto.setProposito(version.getProposito());
        dto.setModoCalificacion(
                version.getModoCalificacion()
        );
        dto.setModalidades(version.getModalidades());
        dto.setActaAprobacion(
                version.getActaAprobacion()
        );
        dto.setActaModificacion(
                version.getActaModificacion()
        );
        dto.setObservaciones(version.getObservaciones());
        dto.setIdProgramaDisena(
                version.getIdProgramaDisena()
        );
        dto.setFechaCarga(version.getFechaCarga());

        return dto;
    }
}      
