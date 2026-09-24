package com.certificados.app.repository;

import com.certificados.app.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Integer> {

    List<Log> findByIdUsuario(Integer idUsuario);

    List<Log> findByNombreTabla(String nombreTabla);

    List<Log> findByNombreProceso(String nombreProceso);
}