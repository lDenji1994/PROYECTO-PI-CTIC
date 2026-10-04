package com.certificados.app.repository;

import com.certificados.app.model.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenidoRepository extends JpaRepository<Contenido, Integer> {

    List<Contenido> findByIdVersionDocumento(Integer idVersionDocumento);

    /** Contenidos de una version en el orden en que se registraron. */
    List<Contenido> findByIdVersionDocumentoOrderByOrdenAscIdAsc(Integer idVersionDocumento);

    /** Borra los contenidos de una version (se usa al corregir los datos del curso). */
    void deleteByIdVersionDocumento(Integer idVersionDocumento);
}
