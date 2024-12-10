package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;

@CircuitBreaker(name = "customTaskRepository")
public interface CustomTaskRepository extends JpaRepository<CustomTaskEntity, String> {
	List<CustomTaskEntity> findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(String id, String municipalityId);

	int countByPhaseId(String phaseId);
}
