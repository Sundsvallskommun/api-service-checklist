package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

@CircuitBreaker(name = "taskRepository")
public interface TaskRepository extends JpaRepository<TaskEntity, String> {
	int countByPhaseId(String phaseId);
}
