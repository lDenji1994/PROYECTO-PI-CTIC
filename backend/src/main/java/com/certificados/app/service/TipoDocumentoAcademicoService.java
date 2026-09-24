package com.certificados.app.service;

import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.repository.TipoDocumentoAcademicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDocumentoAcademicoService {

    private final TipoDocumentoAcademicoRepository repository;

    public TipoDocumentoAcademicoService(TipoDocumentoAcademicoRepository repository) {
        this.repository = repository;
    }

    public List<TipoDocumentoAcademico> listarTodos() {
        return repository.findAll();
    }

    public TipoDocumentoAcademico buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public TipoDocumentoAcademico buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).orElse(null);
    }
}