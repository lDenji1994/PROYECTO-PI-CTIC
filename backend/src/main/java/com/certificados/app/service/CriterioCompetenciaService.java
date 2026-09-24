package com.certificados.app.service;

import com.certificados.app.model.CriterioCompetencia;
import com.certificados.app.repository.CriterioCompetenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CriterioCompetenciaService {

    private final CriterioCompetenciaRepository repository;

    public CriterioCompetenciaService(CriterioCompetenciaRepository repository) {
        this.repository = repository;
    }

    public List<CriterioCompetencia> listarTodos() {
        return repository.findAll();
    }

    public CriterioCompetencia buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<CriterioCompetencia> listarPorCompetencia(Integer idCompetencia) {
        return repository.findByIdCompetencia(idCompetencia);
    }
}