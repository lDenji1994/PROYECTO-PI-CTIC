package com.certificados.app.repository;

import com.certificados.app.model.Log;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Integer> {

    List<Log> findByIdUsuario(Integer idUsuario);

    List<Log> findByNombreTabla(String nombreTabla);

    List<Log> findByNombreProceso(String nombreProceso);

    /* ---- Usados por la "Actividad reciente" del Dashboard ---- */

    /** Ultimos registros de la bitacora, del mas reciente al mas antiguo. */
    List<Log> findAllByOrderByFechaInicioDescIdDesc(Pageable pageable);

    /** Ultimos registros de un grupo de tablas (un modulo del panel). */
    List<Log> findByNombreTablaInOrderByFechaInicioDescIdDesc(Collection<String> tablas, Pageable pageable);
}
