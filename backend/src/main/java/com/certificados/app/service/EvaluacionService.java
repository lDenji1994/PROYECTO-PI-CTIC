package com.certificados.app.service;

import com.certificados.app.model.Evaluacion;
import com.certificados.app.repository.EvaluacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluacionService {

    private final EvaluacionRepository repository;

    public EvaluacionService(EvaluacionRepository repository) {
        this.repository = repository;
    }

    public List<Evaluacion> listarTodos() {
        return repository.findAll();
    }

    public Evaluacion buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Evaluacion> listarPorVersionDocumento(Integer idVersionDocumento) {
        return repository.findByIdVersionDocumento(idVersionDocumento);
    }
}