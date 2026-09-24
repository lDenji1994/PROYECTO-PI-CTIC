package com.certificados.app.repository;

import com.certificados.app.model.CriterioCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterioCompetenciaRepository
        extends JpaRepository<CriterioCompetencia, Integer> {

    List<CriterioCompetencia> findByIdCompetencia(Integer idCompetencia);
}