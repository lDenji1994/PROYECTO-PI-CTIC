package com.certificados.app.repository;

import com.certificados.app.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoAcademicoRepository extends JpaRepository<Documento, String> {

   
    List<Documento> findByTipoDocumento(String tipoDocumento);
}