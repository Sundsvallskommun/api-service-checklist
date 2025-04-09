package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.checklist.integration.db.model.InitiationInfoEntity;

@Repository
@CircuitBreaker(name = "initiationRepository")
public interface InitiationRepository extends JpaRepository<InitiationInfoEntity, String> {
	List<InitiationInfoEntity> findAllByMunicipalityId(String municipalityId);

	void deleteAllByCreatedBefore(OffsetDateTime timestamp);
}
