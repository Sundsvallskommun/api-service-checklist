package se.sundsvall.checklist.integration.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;

@CircuitBreaker(name = "managerRepository")
public interface ManagerRepository extends JpaRepository<ManagerEntity, String> {
}
