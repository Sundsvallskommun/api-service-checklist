package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;

@CircuitBreaker(name = "checklistRepository")
public interface ChecklistRepository extends JpaRepository<ChecklistEntity, String> {

	Optional<ChecklistEntity> findByNameAndMunicipalityIdAndLifeCycle(final String name, final String municipalityId, final LifeCycle lifeCycle);

	boolean existsByNameAndMunicipalityId(final String name, final String municipalityId);

	boolean existsByNameAndMunicipalityIdAndLifeCycle(final String name, final String municipalityId, final LifeCycle lifeCycle);

	boolean existsByIdAndMunicipalityId(final String id, final String municipalityId);

	List<ChecklistEntity> findAllByMunicipalityId(final String municipalityId);

	Optional<ChecklistEntity> findByIdAndMunicipalityId(final String id, final String municipalityId);

}
