package com.certificados.app.repository;

import com.certificados.app.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    List<Evaluacion> findByIdVersionDocumento(Integer idVersionDocumento);
}