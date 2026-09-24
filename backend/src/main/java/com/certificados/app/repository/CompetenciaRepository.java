package com.certificados.app.repository;

import com.certificados.app.model.Competencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Integer> {

    List<Competencia> findByIdVersionDocumento(Integer idVersionDocumento);
}