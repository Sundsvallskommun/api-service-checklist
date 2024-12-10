package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;

@CircuitBreaker(name = "phaseRepository")
public interface PhaseRepository extends JpaRepository<PhaseEntity, String> {

	List<PhaseEntity> findAllByMunicipalityId(final String municipalityId);

	Optional<PhaseEntity> findByIdAndMunicipalityId(final String id, final String municipalityId);

	boolean existsByIdAndMunicipalityId(final String id, final String municipalityId);
}
