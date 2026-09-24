package com.certificados.app.service;

import com.certificados.app.model.Contenido;
import com.certificados.app.repository.ContenidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContenidoService {

    private final ContenidoRepository repository;

    public ContenidoService(ContenidoRepository repository) {
        this.repository = repository;
    }

    public List<Contenido> listarTodos() {
        return repository.findAll();
    }

    public Contenido buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<Contenido> listarPorVersionDocumento(Integer idVersionDocumento) {
        return repository.findByIdVersionDocumento(idVersionDocumento);
    }
}