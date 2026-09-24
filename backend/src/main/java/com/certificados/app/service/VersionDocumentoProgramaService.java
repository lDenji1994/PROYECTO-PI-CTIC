package com.certificados.app.service;

import com.certificados.app.model.VersionDocumentoPrograma;
import com.certificados.app.model.VersionDocumentoProgramaId;
import com.certificados.app.repository.VersionDocumentoProgramaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionDocumentoProgramaService {

    private final VersionDocumentoProgramaRepository repository;

    public VersionDocumentoProgramaService(
            VersionDocumentoProgramaRepository repository) {
        this.repository = repository;
    }

    public List<VersionDocumentoPrograma> listarTodos() {
        return repository.findAll();
    }

    public VersionDocumentoPrograma buscarPorId(
            Integer idVersionDocumento,
            Integer idPrograma) {

        VersionDocumentoProgramaId id =
                new VersionDocumentoProgramaId(
                        idVersionDocumento,
                        idPrograma
                );

        return repository.findById(id).orElse(null);
    }

    public List<VersionDocumentoPrograma> listarPorVersionDocumento(
            Integer idVersionDocumento) {

        return repository.findByIdVersionDocumento(idVersionDocumento);
    }

    public List<VersionDocumentoPrograma> listarPorPrograma(
            Integer idPrograma) {

        return repository.findByIdPrograma(idPrograma);
    }
}