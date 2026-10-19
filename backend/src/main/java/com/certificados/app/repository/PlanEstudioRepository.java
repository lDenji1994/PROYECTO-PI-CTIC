package com.certificados.app.repository;

import com.certificados.app.model.PlanEstudio;
import com.certificados.app.model.PlanEstudioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanEstudioRepository extends JpaRepository<PlanEstudio, PlanEstudioId> {

    List<PlanEstudio> findByIdPlanOrderBySemestreAscIdAsignaturaAsc(Integer idPlan);

    List<PlanEstudio> findByIdPlanAndSemestreIn(Integer idPlan, Collection<Integer> semestres);

    Optional<PlanEstudio> findByIdPlanAndIdAsignatura(Integer idPlan, Integer idAsignatura);

    long countByIdPlan(Integer idPlan);
}
