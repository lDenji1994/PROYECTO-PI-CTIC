package com.certificados.app.service;

import com.certificados.app.model.Log;
import com.certificados.app.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LogService {

    private final LogRepository repository;

    public LogService(LogRepository repository) {
        this.repository = repository;
    }

    public List<Log> listarTodos() {
        return repository.findAll();
    }

    public Log buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Log no encontrado con id " + id
                        ));
    }

    public List<Log> listarPorUsuario(Integer idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public List<Log> listarPorTabla(String nombreTabla) {
        return repository.findByNombreTabla(nombreTabla);
    }

    public List<Log> listarPorProceso(String nombreProceso) {
        return repository.findByNombreProceso(nombreProceso);
    }

    public Log iniciar(
            String ip,
            String nombreTabla,
            String nombreProceso,
            Integer idUsuario) {

        Log log = new Log();

        log.setIp(ip);
        log.setNombreTabla(nombreTabla);
        log.setNombreProceso(nombreProceso);
        log.setIdUsuario(idUsuario);
        log.setFechaInicio(LocalDateTime.now());

        return repository.save(log);
    }

    public Log finalizar(Integer id) {

        Log log = buscarPorId(id);

        log.setFechaFin(LocalDateTime.now());

        return repository.save(log);
    }
}