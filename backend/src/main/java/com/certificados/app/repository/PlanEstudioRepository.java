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

    List<PlanEstudio> findByIdProgramaOrderBySemestreAscIdAsignaturaAsc(Integer idPrograma);

    List<PlanEstudio> findByIdProgramaAndSemestreIn(Integer idPrograma, Collection<Integer> semestres);

    Optional<PlanEstudio> findByIdProgramaAndIdAsignatura(Integer idPrograma, Integer idAsignatura);
}
