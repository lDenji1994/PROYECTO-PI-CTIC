package com.certificados.app.service;

import com.certificados.app.model.Objetivo;
import com.certificados.app.repository.ObjetivoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObjetivoService {

    private final ObjetivoRepository repository;

    public ObjetivoService(ObjetivoRepository repository) {
        this.repository = repository;
    }

    public List<Objetivo> listarTodos() {
        return repository.findAll();
    }

    public Objetivo buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Objetivo> listarPorVersionDocumento(Integer idVersionDocumento) {
        return repository.findByIdVersionDocumento(idVersionDocumento);
    }
}