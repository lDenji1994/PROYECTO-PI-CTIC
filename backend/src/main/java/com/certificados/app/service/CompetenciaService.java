package com.certificados.app.service;

import com.certificados.app.model.Competencia;
import com.certificados.app.repository.CompetenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetenciaService {

    private final CompetenciaRepository repository;

    public CompetenciaService(CompetenciaRepository repository) {
        this.repository = repository;
    }

    public List<Competencia> listarTodos() {
        return repository.findAll();
    }

    public Competencia buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Competencia> listarPorVersionDocumento(Integer idVersionDocumento) {
        return repository.findByIdVersionDocumento(idVersionDocumento);
    }
}