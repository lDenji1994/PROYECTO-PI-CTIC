package com.certificados.app.repository;

import com.certificados.app.model.Metodologia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodologiaRepository extends JpaRepository<Metodologia, Integer> {

    List<Metodologia> findByIdVersionDocumento(Integer idVersionDocumento);
}