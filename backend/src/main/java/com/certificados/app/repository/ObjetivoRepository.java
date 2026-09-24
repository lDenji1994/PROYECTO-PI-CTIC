package com.certificados.app.repository;

import com.certificados.app.model.Objetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjetivoRepository extends JpaRepository<Objetivo, Integer> {

    List<Objetivo> findByIdVersionDocumento(Integer idVersionDocumento);
}