package com.certificados.app.service;

import com.certificados.app.dto.SolicitudCertificadoDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.EstadoSolicitudCertificado;
import com.certificados.app.model.Log;
import com.certificados.app.model.PlantillaCertificado;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.LogRepository;
import com.certificados.app.repository.PlantillaCertificadoRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.TipoCertificadoRepository;
import com.certificados.app.repository.UsuarioRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    public SolicitudCertificadoService(
            SolicitudCertificadoRepository repository,
            AsignaturaRepository asignaturaRepository,
            DetalleSolicitudCertificadoRepository detalleRepository,
            TipoCertificadoRepository tipoCertificadoRepository,
            UsuarioRepository usuarioRepository,
            PlantillaCertificadoRepository plantillaCertificadoRepository,
            VersionPlantillaRepository versionPlantillaRepository,
            CertificadoGeneradorService certificadoGeneradorService,
            LogRepository logRepository) {

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

        SolicitudCertificado solicitud =
                new SolicitudCertificado();

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

        return toDTO(repository.save(solicitud));
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

        solicitud.setEstado(
                EstadoSolicitudCertificado.PROCESANDO
        );

        solicitud.setFechaInicioProcesamiento(
                LocalDateTime.now()
        );

        repository.save(solicitud);

        Log log = new Log();

        log.setIp("127.0.0.1");
        log.setNombreTabla("CertificadosGeneradosS");
        log.setNombreProceso("GENERAR_CERTIFICADO");

        log.setIdUsuario(
                solicitud.getIdUsuarioEncargado()
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

            throw new BusinessException(
                    "No fue posible generar el certificado: "
                            + e.getMessage()
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

        return toDTO(repository.save(solicitud));
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

        return toDTO(repository.save(solicitud));
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

        return toDTO(repository.save(solicitud));
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

            case REALIZADO, ERROR ->
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

        return dto;
    }
}