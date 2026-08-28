package com.certificados.app.service;

import com.certificados.app.dto.CertificadoDTO;
import com.certificados.app.dto.HistorialEstadoCertificadoDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Certificado;
import com.certificados.app.model.EstadoCertificado;
import com.certificados.app.model.Estudiante;
import com.certificados.app.model.HistorialEstadoCertificado;
import com.certificados.app.repository.CertificadoRepository;
import com.certificados.app.repository.EstudianteRepository;
import com.certificados.app.repository.HistorialEstadoCertificadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final EstudianteRepository estudianteRepository;
    private final HistorialEstadoCertificadoRepository historialRepository;

    public List<CertificadoDTO> listarTodos() {
        return certificadoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CertificadoDTO> listarPorEstado(EstadoCertificado estado) {
    return certificadoRepository.findByEstado(estado).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<CertificadoDTO> listarPorEstudiante(Long estudianteId) {
        return certificadoRepository.findByEstudianteId(estudianteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CertificadoDTO buscarPorId(Long id) {
        return toDTO(obtenerEntidad(id));
    }

    // US-21.03: Implementar consulta de historial
    public List<HistorialEstadoCertificadoDTO> listarHistorial(Long id) {
        obtenerEntidad(id); // valida que el certificado exista (404 si no)
        return historialRepository.findByCertificadoIdOrderByFechaCambioAsc(id).stream()
                .map(this::toHistorialDTO)
                .collect(Collectors.toList());
    }

    public CertificadoDTO solicitar(CertificadoDTO dto) {
        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante no encontrado con id " + dto.getEstudianteId()));

        Certificado certificado = new Certificado();
        certificado.setTipo(dto.getTipo());
        certificado.setEstudiante(estudiante);
        certificado.setEstado(EstadoCertificado.PENDIENTE);
        certificado.setFechaSolicitud(LocalDate.now());
        certificado.setObservaciones(dto.getObservaciones());
        certificado.setCodigoVerificacion(generarCodigoVerificacion());

        return toDTO(certificadoRepository.save(certificado));
    }

    public CertificadoDTO emitir(Long id) {
        Certificado certificado = obtenerEntidad(id);
        validarTransicion(certificado.getEstado(), EstadoCertificado.EMITIDO);
        EstadoCertificado estadoAnterior = certificado.getEstado();
        certificado.setEstado(EstadoCertificado.EMITIDO);
        certificado.setFechaEmision(LocalDate.now());
        Certificado actualizado = certificadoRepository.save(certificado);
        registrarCambioEstado(actualizado, estadoAnterior, EstadoCertificado.EMITIDO);
        return toDTO(actualizado);
    }

    public CertificadoDTO anular(Long id) {
        Certificado certificado = obtenerEntidad(id);
        validarTransicion(certificado.getEstado(), EstadoCertificado.ANULADO);
        EstadoCertificado estadoAnterior = certificado.getEstado();
        certificado.setEstado(EstadoCertificado.ANULADO);
        Certificado actualizado = certificadoRepository.save(certificado);
        registrarCambioEstado(actualizado, estadoAnterior, EstadoCertificado.ANULADO);
        return toDTO(actualizado);
    }

    /**
     * US-20.05: Validar transiciones permitidas.
     * Reglas: un certificado ANULADO no puede cambiar de estado nunca mas,
     * y no tiene sentido "cambiar" a un estado en el que ya esta.
     * PENDIENTE -> EMITIDO, PENDIENTE -> ANULADO y EMITIDO -> ANULADO si se permiten.
     */
    private void validarTransicion(EstadoCertificado actual, EstadoCertificado destino) {
        if (actual == EstadoCertificado.ANULADO) {
            throw new BusinessException("No se puede cambiar el estado de un certificado anulado");
        }
        if (actual == destino) {
            throw new BusinessException("El certificado ya se encuentra en estado " + destino);
        }
    }

    private Certificado obtenerEntidad(Long id) {
        return certificadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado con id " + id));
    }

    private String generarCodigoVerificacion() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void registrarCambioEstado(Certificado certificado, EstadoCertificado anterior, EstadoCertificado nuevo) {
        // Si por algun motivo el estado no cambio realmente (ej. anular un
        // certificado que ya estaba ANULADO), no vale la pena dejar un
        // registro de historial redundante.
        if (anterior == nuevo) {
            return;
        }
        HistorialEstadoCertificado registro = new HistorialEstadoCertificado();
        registro.setCertificado(certificado);
        registro.setEstadoAnterior(anterior);
        registro.setEstadoNuevo(nuevo);
        historialRepository.save(registro);
    }

    private HistorialEstadoCertificadoDTO toHistorialDTO(HistorialEstadoCertificado h) {
        HistorialEstadoCertificadoDTO dto = new HistorialEstadoCertificadoDTO();
        dto.setId(h.getId());
        dto.setCertificadoId(h.getCertificado().getId());
        dto.setEstadoAnterior(h.getEstadoAnterior());
        dto.setEstadoNuevo(h.getEstadoNuevo());
        dto.setFechaCambio(h.getFechaCambio());
        return dto;
    }

    private CertificadoDTO toDTO(Certificado c) {
        CertificadoDTO dto = new CertificadoDTO();
        dto.setId(c.getId());
        dto.setCodigoVerificacion(c.getCodigoVerificacion());
        dto.setTipo(c.getTipo());
        dto.setEstado(c.getEstado());
        dto.setFechaSolicitud(c.getFechaSolicitud());
        dto.setFechaEmision(c.getFechaEmision());
        dto.setObservaciones(c.getObservaciones());
        dto.setEstudianteId(c.getEstudiante().getId());
        dto.setNombreEstudiante(c.getEstudiante().getNombres() + " " + c.getEstudiante().getApellidos());
        return dto;
    }
}
