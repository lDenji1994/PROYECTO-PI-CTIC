package com.certificados.app.service;

import com.certificados.app.dto.VersionResumenDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.Competencia;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.CompetenciaRepository;
import com.certificados.app.repository.ContenidoRepository;
import com.certificados.app.repository.CriterioCompetenciaRepository;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.EvaluacionRepository;
import com.certificados.app.repository.LogRepository;
import com.certificados.app.repository.MetodologiaRepository;
import com.certificados.app.repository.ProgramaRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.CertificadoGeneradoRepository;
import com.certificados.app.repository.UsuarioRepository;
import com.certificados.app.security.UsuarioDetallesService;
import com.certificados.app.security.UsuarioSesion;
import com.certificados.app.model.CertificadoGenerado;
import com.certificados.app.model.Programa;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.Usuario;
import com.certificados.app.repository.ObjetivoRepository;
import com.certificados.app.repository.VersionDocumentoProgramaRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ELIMINAR DESDE EL PANEL LO QUE SE SUBIO O REGISTRO POR ERROR
 * (para no tener que borrarlo a mano en la base de datos).
 *
 *  - eliminarVersion:    borra un documento cargado (el archivo y los datos
 *                        del curso que se registraron con el).
 *  - eliminarAsignatura: borra una asignatura que ya no tiene nada asociado.
 *  - eliminarPrograma:   borra un programa que ningun documento usa.
 *  - eliminarSolicitud:  borra una solicitud con sus asignaturas y, si lo
 *                        tiene, el PDF del certificado.
 *  - eliminarUsuario:    borra una cuenta creada por error (sin actividad).
 *
 * Reglas:
 *  - Todo ocurre en una sola transaccion: o se borra completo o no se borra
 *    nada (no quedan registros "huerfanos").
 *  - Si era el unico archivo de ese documento, tambien se quita el
 *    documento de la lista. Si habia versiones anteriores, se conservan.
 *  - Los certificados YA generados no cambian: su PDF esta guardado aparte.
 *  - Una asignatura con documentos o incluida en una solicitud NO se
 *    elimina (se avisa que hay que quitar eso primero).
 *  - Un programa usado por algun documento NO se elimina.
 *  - Una solicitud que YA tiene certificado generado solo la puede eliminar
 *    el administrador (ese PDF es el registro de lo que se entrego).
 *  - Un usuario con actividad registrada NO se elimina (la bitacora quedaria
 *    sin autor): se desactiva desde el menu Usuarios.
 *  - Cada eliminacion queda en la bitacora con el usuario que la hizo.
 *
 * NO MODIFICAR: el orden de borrado en eliminarVersion. La base de datos
 * no deja borrar una version mientras existan filas que dependan de ella.
 * SE PUEDE MODIFICAR: los mensajes y las reglas de eliminarAsignatura.
 */
@Service
@Transactional
public class EliminacionService {

    private final VersionDocumentoRepository versionRepository;
    private final DocumentoAcademicoRepository documentoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final ContenidoRepository contenidoRepository;
    private final ObjetivoRepository objetivoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final CriterioCompetenciaRepository criterioRepository;
    private final MetodologiaRepository metodologiaRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final VersionDocumentoProgramaRepository versionProgramaRepository;
    private final DetalleSolicitudCertificadoRepository detalleSolicitudRepository;
    private final ProgramaRepository programaRepository;
    private final SolicitudCertificadoRepository solicitudRepository;
    private final CertificadoGeneradoRepository certificadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogRepository logRepository;
    private final ActividadService actividadService;

    public EliminacionService(
            VersionDocumentoRepository versionRepository,
            DocumentoAcademicoRepository documentoRepository,
            AsignaturaRepository asignaturaRepository,
            ContenidoRepository contenidoRepository,
            ObjetivoRepository objetivoRepository,
            CompetenciaRepository competenciaRepository,
            CriterioCompetenciaRepository criterioRepository,
            MetodologiaRepository metodologiaRepository,
            EvaluacionRepository evaluacionRepository,
            VersionDocumentoProgramaRepository versionProgramaRepository,
            DetalleSolicitudCertificadoRepository detalleSolicitudRepository,
            ProgramaRepository programaRepository,
            SolicitudCertificadoRepository solicitudRepository,
            CertificadoGeneradoRepository certificadoRepository,
            UsuarioRepository usuarioRepository,
            LogRepository logRepository,
            ActividadService actividadService) {
        this.programaRepository = programaRepository;
        this.solicitudRepository = solicitudRepository;
        this.certificadoRepository = certificadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logRepository = logRepository;
        this.versionRepository = versionRepository;
        this.documentoRepository = documentoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.contenidoRepository = contenidoRepository;
        this.objetivoRepository = objetivoRepository;
        this.competenciaRepository = competenciaRepository;
        this.criterioRepository = criterioRepository;
        this.metodologiaRepository = metodologiaRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.versionProgramaRepository = versionProgramaRepository;
        this.detalleSolicitudRepository = detalleSolicitudRepository;
        this.actividadService = actividadService;
    }

    /** Elimina un documento cargado (una version) con todo lo que depende de el. */
    public void eliminarVersion(Integer idVersion) {
        VersionResumenDTO version = versionRepository.buscarResumen(idVersion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El documento ya no existe (id " + idVersion + "). Actualiza la página."));

        // 1. Lo que depende de la version
        contenidoRepository.deleteByIdVersionDocumento(idVersion);
        objetivoRepository.deleteByIdVersionDocumento(idVersion);
        metodologiaRepository.deleteByIdVersionDocumento(idVersion);
        evaluacionRepository.deleteByIdVersionDocumento(idVersion);
        versionProgramaRepository.deleteByIdVersionDocumento(idVersion);
        for (Competencia competencia : competenciaRepository.findByIdVersionDocumento(idVersion)) {
            criterioRepository.deleteByIdCompetencia(competencia.getId());
        }
        criterioRepository.flush();
        competenciaRepository.deleteByIdVersionDocumento(idVersion);
        // flush(): envia YA a la base de datos todos los borrados anteriores.
        // Sin esto, el borrado de la version (paso 2) se ejecutaria primero y
        // la base de datos lo rechazaria por las filas que aun dependen de ella.
        competenciaRepository.flush();

        // 2. La version (archivo + datos del curso)
        versionRepository.eliminarPorId(idVersion);

        // 3. Si el documento se quedo sin archivos, tambien se quita
        Integer idDocumento = version.idDocumentoAcademico();
        DocumentoAcademico documento = documentoRepository.findById(idDocumento).orElse(null);
        boolean quedanVersiones = versionRepository.countByIdDocumentoAcademico(idDocumento) > 0;
        if (documento != null && !quedanVersiones) {
            documentoRepository.delete(documento);
        }

        String asignatura = documento == null ? "" : asignaturaRepository.findById(documento.getIdAsignatura())
                .map(a -> a.getCodigo() + " - " + a.getNombre() + ": ")
                .orElse("");
        actividadService.registrar(ActividadService.TABLA_VERSIONES, ActividadService.ELIMINAR_DOCUMENTO,
                asignatura + version.nombreArchivo()
                        + (version.periodo() == null ? "" : " (periodo " + version.periodo() + ")"));
    }

    /** Elimina una asignatura registrada por error (solo si no tiene nada asociado). */
    public void eliminarAsignatura(Integer idAsignatura) {
        Asignatura asignatura = asignaturaRepository.findById(idAsignatura)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La asignatura ya no existe (id " + idAsignatura + "). Actualiza la página."));

        if (!documentoRepository.findByIdAsignatura(idAsignatura).isEmpty()) {
            throw new BusinessException("La asignatura " + asignatura.getCodigo()
                    + " tiene documentos cargados. Elimínalos primero en Carga de información.");
        }
        if (detalleSolicitudRepository.existsByIdAsignatura(idAsignatura)) {
            throw new BusinessException("La asignatura " + asignatura.getCodigo()
                    + " está incluida en una solicitud de certificado y no se puede eliminar.");
        }

        asignaturaRepository.delete(asignatura);
        actividadService.registrar(ActividadService.TABLA_ASIGNATURAS, ActividadService.ELIMINAR_ASIGNATURA,
                asignatura.getCodigo() + " - " + asignatura.getNombre());
    }

    /** Elimina un programa academico que ningun documento cargado usa. */
    public void eliminarPrograma(Integer idPrograma) {
        Programa programa = programaRepository.findById(idPrograma)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El programa ya no existe (id " + idPrograma + "). Actualiza la página."));

        if (versionRepository.existsByIdProgramaDisena(idPrograma)
                || !versionProgramaRepository.findByIdPrograma(idPrograma).isEmpty()) {
            throw new BusinessException("El programa " + programa.getCodigo()
                    + " está asociado a documentos cargados y no se puede eliminar.");
        }

        programaRepository.delete(programa);
        actividadService.registrar(ActividadService.TABLA_PROGRAMAS, ActividadService.ELIMINAR_PROGRAMA,
                programa.getCodigo() + " - " + programa.getNombre());
    }

    /** Elimina una solicitud con sus asignaturas y su certificado (si ya se genero). */
    public void eliminarSolicitud(Integer idSolicitud) {
        SolicitudCertificado solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La solicitud ya no existe (id " + idSolicitud + "). Actualiza la página."));

        CertificadoGenerado certificado =
                certificadoRepository.findByIdSolicitudCertificado(idSolicitud).orElse(null);
        boolean esAdministrador = UsuarioDetallesService.actual()
                .map(UsuarioSesion::esAdministrador)
                .orElse(false);
        if (certificado != null && !esAdministrador) {
            throw new BusinessException("La solicitud #" + idSolicitud
                    + " ya tiene un certificado generado. Solo el administrador puede eliminarla.");
        }

        // Orden obligatorio: primero lo que depende de la solicitud
        if (certificado != null) {
            certificadoRepository.delete(certificado);
        }
        detalleSolicitudRepository.deleteByIdSolicitudCertificado(idSolicitud);
        detalleSolicitudRepository.flush();
        solicitudRepository.delete(solicitud);

        actividadService.registrar(ActividadService.TABLA_SOLICITUDES, ActividadService.ELIMINAR_SOLICITUD,
                "#" + idSolicitud + " estudiante " + solicitud.getIdEstudiante()
                        + (certificado != null ? " (con su certificado PDF)" : ""));
    }

    /** Elimina una cuenta creada por error. Con actividad registrada, se desactiva en vez de eliminar. */
    public void eliminarUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario ya no existe (id " + idUsuario + "). Actualiza la página."));

        boolean esElMismo = UsuarioDetallesService.actual()
                .map(sesion -> sesion.getId().equals(idUsuario))
                .orElse(false);
        if (esElMismo) {
            throw new BusinessException("No puedes eliminar tu propia cuenta.");
        }
        if (logRepository.existsByIdUsuario(idUsuario)
                || solicitudRepository.existsByIdUsuarioEncargado(idUsuario)) {
            throw new BusinessException("El usuario " + usuario.getUsuario()
                    + " ya tiene actividad o solicitudes registradas. Para que no pueda entrar, desactívalo.");
        }

        usuarioRepository.delete(usuario);
        actividadService.registrar(ActividadService.TABLA_USUARIOS, ActividadService.ELIMINAR_USUARIO,
                usuario.getUsuario() + " - " + usuario.getNombreCompleto());
    }
}
