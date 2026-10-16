package com.certificados.app.repository;

import com.certificados.app.model.DocumentoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoAcademicoRepository
        extends JpaRepository<DocumentoAcademico, Integer> {

    List<DocumentoAcademico> findByIdAsignatura(Integer idAsignatura);
}
