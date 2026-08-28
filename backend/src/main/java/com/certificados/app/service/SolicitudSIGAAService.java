package com.certificados.app.service;

import com.certificados.app.dto.SolicitudSIGAADTO;
import com.certificados.app.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SolicitudSIGAAService {

    // Registro temporal de solicitudes recibidas.
    // Será reemplazado por persistencia en BD.
    private final List<SolicitudSIGAADTO> solicitudesRecibidas = new ArrayList<>();

    public SolicitudSIGAADTO recibirSolicitud(SolicitudSIGAADTO solicitud) {

        // Validar que la solicitud no exista previamente.
        boolean duplicada = solicitudesRecibidas.stream()
                .anyMatch(s -> s.getSolicitudId().equals(solicitud.getSolicitudId()));

        if (duplicada) {
            throw new BusinessException(
                    "La solicitud SIGAA " + solicitud.getSolicitudId()
                            + " ya fue registrada"
            );
        }

        // Información generada por nuestro sistema.
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaRecepcion(LocalDateTime.now().toString());

        // Registrar temporalmente la solicitud.
        solicitudesRecibidas.add(solicitud);

        return solicitud;
    }

    public List<SolicitudSIGAADTO> listarSolicitudes() {
        return new ArrayList<>(solicitudesRecibidas);
    }

    public SolicitudSIGAADTO buscarPorSolicitudId(String solicitudId) {

        return solicitudesRecibidas.stream()
                .filter(s -> s.getSolicitudId().equals(solicitudId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No se encontró la solicitud SIGAA " + solicitudId
                ));
    }
}