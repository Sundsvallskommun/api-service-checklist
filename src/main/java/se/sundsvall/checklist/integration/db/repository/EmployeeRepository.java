package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;

@Repository
@CircuitBreaker(name = "employeeRepository")
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {
}
