package se.sundsvall.checklist.integration.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;

@CircuitBreaker(name = "phaseRepository")
public interface PhaseRepository extends JpaRepository<PhaseEntity, String> {
}
