package com.certificados.app.service;

import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentoAcademicoService {

    private final DocumentoAcademicoRepository repository;

    public DocumentoAcademicoService(
            DocumentoAcademicoRepository repository) {
        this.repository = repository;
    }

    public List<DocumentoAcademico> listarTodos() {
        return repository.findAll();
    }

    public DocumentoAcademico buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Documento académico no encontrado con id " + id
                        ));
    }

    public List<DocumentoAcademico> listarPorAsignatura(
            Integer idAsignatura) {
        return repository.findByIdAsignatura(idAsignatura);
    }

    public DocumentoAcademico guardar(
            DocumentoAcademico documento) {

        return repository.save(documento);
    }
}
