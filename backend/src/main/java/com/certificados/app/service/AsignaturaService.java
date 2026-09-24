package com.certificados.app.service;

import com.certificados.app.model.Asignatura;
import com.certificados.app.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AsignaturaService {

    private final AsignaturaRepository repository;

    public AsignaturaService(AsignaturaRepository repository) {
        this.repository = repository;
    }

    public List<Asignatura> listarTodas() {
        return repository.findAll();
    }

    public Asignatura buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Asignatura no encontrada con id " + id
                        ));
    }

    public Asignatura buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe una asignatura con código " +
                                codigo
                        ));
    }

    public Asignatura crear(Asignatura asignatura) {
        return repository.save(asignatura);
    }
}
