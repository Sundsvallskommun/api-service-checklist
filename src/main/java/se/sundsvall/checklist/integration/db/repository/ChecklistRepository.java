package se.sundsvall.checklist.integration.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;

@CircuitBreaker(name = "checklistRepository")
public interface ChecklistRepository extends JpaRepository<ChecklistEntity, String> {

	Optional<ChecklistEntity> findByNameAndLifeCycle(final String name, final LifeCycle lifeCycle);

	boolean existsByName(final String name);

	boolean existsByNameAndLifeCycle(final String name, final LifeCycle lifeCycle);

}
