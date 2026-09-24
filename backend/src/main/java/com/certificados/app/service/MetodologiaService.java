package com.certificados.app.service;

import com.certificados.app.model.Metodologia;
import com.certificados.app.repository.MetodologiaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodologiaService {

    private final MetodologiaRepository repository;

    public MetodologiaService(MetodologiaRepository repository) {
        this.repository = repository;
    }

    public List<Metodologia> listarTodos() {
        return repository.findAll();
    }

    public Metodologia buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Metodologia> listarPorVersionDocumento(Integer idVersionDocumento) {
        return repository.findByIdVersionDocumento(idVersionDocumento);
    }
}