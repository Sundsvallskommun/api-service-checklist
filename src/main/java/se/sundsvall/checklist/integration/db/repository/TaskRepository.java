package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

@Repository
@CircuitBreaker(name = "taskRepository")
public interface TaskRepository extends JpaRepository<TaskEntity, String> {
	int countByPhaseId(String phaseId);
}
