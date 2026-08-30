package com.certificados.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.certificados.app.model.Documento;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long>{
   Optional<Documento> findByRutaAlmacenamiento(String rutaAlmacenamiento);
}