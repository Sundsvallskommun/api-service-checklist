package se.sundsvall.checklist.integration.db.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

@CircuitBreaker(name = "employeeChecklistRepository")
public interface EmployeeChecklistRepository extends JpaRepository<EmployeeChecklistEntity, String>, JpaSpecificationExecutor<EmployeeChecklistEntity> {
	List<EmployeeChecklistEntity> findAllByCorrespondenceIsNull();

	List<EmployeeChecklistEntity> findAllByCorrespondenceCorrespondenceStatus(CorrespondenceStatus status);

	EmployeeChecklistEntity findByEmployeeUsername(String username);

	List<EmployeeChecklistEntity> findAllByEmployeeManagerUsername(String username);

	List<EmployeeChecklistEntity> findAllByExpirationDateIsBeforeAndLockedIsFalse(LocalDate date);
}
