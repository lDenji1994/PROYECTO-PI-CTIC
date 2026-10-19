package com.certificados.app.repository;

import com.certificados.app.model.EstadoPlanEstudio;
import com.certificados.app.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {

    List<Plan> findByIdPrograma(Integer idPrograma);

    List<Plan> findByIdProgramaAndEstado(Integer idPrograma, EstadoPlanEstudio estado);

    boolean existsByIdProgramaAndNombreIgnoreCase(Integer idPrograma, String nombre);

    boolean existsByIdProgramaAndNombreIgnoreCaseAndIdNot(Integer idPrograma, String nombre, Integer id);
}
