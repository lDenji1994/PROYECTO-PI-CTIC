package com.certificados.app.service;

import com.certificados.app.dto.ActividadDTO;
import com.certificados.app.model.Log;
import com.certificados.app.model.Usuario;
import com.certificados.app.repository.LogRepository;
import com.certificados.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BITACORA DE ACTIVIDAD ("Actividad reciente" del Dashboard).
 *
 * Todas las acciones importantes del sistema (registrar asignaturas,
 * cargar documentos, crear/procesar solicitudes...) llaman a
 * {@link #registrar} y quedan guardadas en la tabla de Logs que ya
 * definio el equipo. El Dashboard las lee con {@link #listarRecientes}.
 *
 * Como la tabla de Logs solo tiene "nombreTabla" y "nombreProceso",
 * el detalle legible se guarda dentro de nombreProceso con el formato:
 *
 *        CODIGO_PROCESO · detalle
 *
 * (maximo 150 caracteres; se recorta automaticamente).
 *
 * SE PUEDE MODIFICAR:
 *   - Los textos de TITULOS y los niveles (colores) de NIVELES.
 *   - Agregar procesos nuevos: se agrega la constante, su titulo y su nivel.
 * NO MODIFICAR:
 *   - El separador " · " (lo usan registrar y listarRecientes).
 *   - Los nombres de tabla ya usados: cambiarlos "pierde" el historial viejo
 *     del filtro por modulo.
 */
@Service
public class ActividadService {

    private static final Logger LOG = LoggerFactory.getLogger(ActividadService.class);

    private static final String SEPARADOR = " · ";
    private static final int MAX_PROCESO = 150;
    private static final int MAX_POR_CONSULTA = 200;

    /* ---------- Tablas (valor que se guarda en t_nombreTabla) ---------- */
    public static final String TABLA_PROGRAMAS = "ProgramasS";
    public static final String TABLA_ASIGNATURAS = "AsignaturasS";
    public static final String TABLA_DOCUMENTOS = "DocumentosAcademicosS";
    public static final String TABLA_VERSIONES = "VersionesDocumentosS";
    public static final String TABLA_SOLICITUDES = "SolicitudesCertificadosS";
    public static final String TABLA_DETALLE_SOLICITUD = "DetallesSolicitudesCertificadosS";
    public static final String TABLA_CERTIFICADOS = "CertificadosGeneradosS";

    /* ---------- Procesos (primera parte de t_nombreProceso) ---------- */
    public static final String CREAR_PROGRAMA = "CREAR_PROGRAMA";
    public static final String CREAR_ASIGNATURA = "CREAR_ASIGNATURA";
    public static final String CREAR_DOCUMENTO = "CREAR_DOCUMENTO_ACADEMICO";
    public static final String ACTUALIZAR_FORMATO = "ACTUALIZAR_FORMATO_DOCUMENTO";
    public static final String CARGAR_DOCUMENTO = "CARGAR_VERSION_DOCUMENTO";
    public static final String CARGAR_DOCUMENTO_ANTIGUO = "CARGAR_DOCUMENTO_FORMATO_ANTIGUO";
    public static final String CREAR_SOLICITUD = "CREAR_SOLICITUD_CERTIFICADO";
    public static final String AGREGAR_ASIGNATURA_SOLICITUD = "AGREGAR_ASIGNATURA_SOLICITUD";
    public static final String SOLICITUD_ESPERANDO = "SOLICITUD_ESPERANDO_DOCUMENTOS";
    public static final String SOLICITUD_REALIZADA = "SOLICITUD_REALIZADA";
    public static final String SOLICITUD_ERROR = "SOLICITUD_ERROR";
    /** Lo registra SolicitudCertificadoService.iniciarProcesamiento (codigo del equipo). */
    public static final String GENERAR_CERTIFICADO = "GENERAR_CERTIFICADO";

    private static final Map<String, String> TITULOS = Map.ofEntries(
            Map.entry(CREAR_PROGRAMA, "Programa académico registrado"),
            Map.entry(CREAR_ASIGNATURA, "Asignatura registrada"),
            Map.entry(CREAR_DOCUMENTO, "Documento académico registrado"),
            Map.entry(ACTUALIZAR_FORMATO, "Formato de documento actualizado"),
            Map.entry(CARGAR_DOCUMENTO, "Nueva versión de documento cargada"),
            Map.entry(CARGAR_DOCUMENTO_ANTIGUO, "Documento cargado con formato desactualizado"),
            Map.entry(CREAR_SOLICITUD, "Solicitud de certificado creada"),
            Map.entry(AGREGAR_ASIGNATURA_SOLICITUD, "Asignatura agregada a una solicitud"),
            Map.entry(SOLICITUD_ESPERANDO, "Solicitud en espera de documentos"),
            Map.entry(SOLICITUD_REALIZADA, "Solicitud marcada como realizada"),
            Map.entry(SOLICITUD_ERROR, "Solicitud marcada con error"),
            Map.entry(GENERAR_CERTIFICADO, "Generación de certificado PDF")
    );

    private static final Map<String, String> NIVELES = Map.of(
            CARGAR_DOCUMENTO_ANTIGUO, "warning",
            SOLICITUD_ESPERANDO, "warning",
            SOLICITUD_ERROR, "danger",
            GENERAR_CERTIFICADO, "info"
    );

    /** Que tablas pertenecen a cada modulo del panel (filtro del Dashboard). */
    private static final Map<String, List<String>> MODULOS = Map.of(
            "documentos", List.of(TABLA_DOCUMENTOS, TABLA_VERSIONES),
            "cursos", List.of(TABLA_ASIGNATURAS, TABLA_PROGRAMAS),
            "certificaciones", List.of(TABLA_SOLICITUDES, TABLA_DETALLE_SOLICITUD, TABLA_CERTIFICADOS)
    );

    private final LogRepository logRepository;
    private final UsuarioRepository usuarioRepository;
    private final Integer idUsuarioPorDefecto;

    public ActividadService(
            LogRepository logRepository,
            UsuarioRepository usuarioRepository,
            @Value("${app.auditoria.id-usuario-por-defecto:1}") Integer idUsuarioPorDefecto) {
        this.logRepository = logRepository;
        this.usuarioRepository = usuarioRepository;
        this.idUsuarioPorDefecto = idUsuarioPorDefecto;
    }

    /* =====================================================================
       REGISTRO
       ===================================================================== */

    /** Registra una accion a nombre del usuario por defecto (mientras no haya login). */
    public void registrar(String tabla, String proceso, String detalle) {
        registrar(tabla, proceso, detalle, null);
    }

    /**
     * Registra una accion ya terminada en la bitacora.
     * Se ejecuta dentro de la misma transaccion de la accion: si la accion
     * falla y se revierte, tampoco queda registro (no hay "falsos" registros).
     *
     * Si el usuario no existe en la BD, la accion NO se bloquea: solo se
     * avisa en consola y no se guarda la bitacora.
     */
    @Transactional
    public void registrar(String tabla, String proceso, String detalle, Integer idUsuario) {
        Integer usuario = idUsuario != null ? idUsuario : idUsuarioPorDefecto;

        if (usuario == null || !usuarioRepository.existsById(usuario)) {
            LOG.warn("Bitacora: el usuario {} no existe; no se registra '{}'. "
                    + "Revisa app.auditoria.id-usuario-por-defecto.", usuario, proceso);
            return;
        }

        Log log = new Log();
        log.setIp(ipActual());
        log.setNombreTabla(tabla);
        log.setNombreProceso(componerProceso(proceso, detalle));
        log.setIdUsuario(usuario);
        LocalDateTime ahora = LocalDateTime.now();
        log.setFechaInicio(ahora);
        log.setFechaFin(ahora);
        logRepository.save(log);
    }

    /** IP de quien hizo la peticion HTTP (o 127.0.0.1 si no hay peticion). */
    public String ipActual() {
        var atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes sra) {
            HttpServletRequest request = sra.getRequest();
            String ip = request.getRemoteAddr();
            if (ip != null && !ip.isBlank()) {
                return ip.length() > 45 ? ip.substring(0, 45) : ip;
            }
        }
        return "127.0.0.1";
    }

    private String componerProceso(String proceso, String detalle) {
        String texto = (detalle == null || detalle.isBlank())
                ? proceso
                : proceso + SEPARADOR + detalle.trim().replaceAll("\\s+", " ");
        return texto.length() > MAX_PROCESO ? texto.substring(0, MAX_PROCESO - 1) + "…" : texto;
    }

    /* =====================================================================
       CONSULTA (Dashboard)
       ===================================================================== */

    /**
     * Ultimas acciones, de la mas reciente a la mas antigua.
     * @param modulo documentos | cursos | certificaciones | null (todas)
     * @param limite cuantas traer (1..200)
     */
    @Transactional(readOnly = true)
    public List<ActividadDTO> listarRecientes(String modulo, int limite) {
        int cantidad = Math.max(1, Math.min(limite, MAX_POR_CONSULTA));
        PageRequest pagina = PageRequest.of(0, cantidad);

        List<Log> logs;
        if (modulo != null && MODULOS.containsKey(modulo)) {
            logs = logRepository.findByNombreTablaInOrderByFechaInicioDescIdDesc(MODULOS.get(modulo), pagina);
        } else if (modulo == null || modulo.isBlank() || "todos".equals(modulo)) {
            logs = logRepository.findAllByOrderByFechaInicioDescIdDesc(pagina);
        } else {
            throw new IllegalArgumentException("Modulo no valido: use documentos, cursos o certificaciones");
        }

        Set<Integer> idsUsuarios = logs.stream().map(Log::getIdUsuario)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> nombres = usuarioRepository.findAllById(idsUsuarios).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombreCompleto, (a, b) -> a));

        return logs.stream().map(l -> aDTO(l, nombres)).toList();
    }

    private ActividadDTO aDTO(Log log, Map<Integer, String> nombres) {
        String completo = log.getNombreProceso() == null ? "" : log.getNombreProceso();
        int corte = completo.indexOf(SEPARADOR);
        String proceso = corte >= 0 ? completo.substring(0, corte) : completo;
        String detalle = corte >= 0 ? completo.substring(corte + SEPARADOR.length()) : "";

        String titulo = TITULOS.getOrDefault(proceso, proceso.replace('_', ' ').toLowerCase(Locale.ROOT));
        String nivel = NIVELES.getOrDefault(proceso, "success");
        String estado = log.getFechaFin() == null ? "EN_CURSO" : "FINALIZADO";

        String modulo = MODULOS.entrySet().stream()
                .filter(e -> e.getValue().contains(log.getNombreTabla()))
                .map(Map.Entry::getKey).findFirst().orElse("otros");

        return new ActividadDTO(
                log.getId(), proceso, titulo, detalle, log.getNombreTabla(), modulo, nivel, estado,
                nombres.getOrDefault(log.getIdUsuario(), "Usuario #" + log.getIdUsuario()),
                log.getIp(), log.getFechaInicio(), log.getFechaFin());
    }
}
