package se.sundsvall.checklist.integration.db.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

@CircuitBreaker(name = "employeeChecklistRepository")
public interface EmployeeChecklistRepository extends JpaRepository<EmployeeChecklistEntity, String>, JpaSpecificationExecutor<EmployeeChecklistEntity> {
	List<EmployeeChecklistEntity> findAllByChecklistMunicipalityIdAndCorrespondenceIsNull(String municipalityId);

	List<EmployeeChecklistEntity> findAllByChecklistMunicipalityIdAndCorrespondenceCorrespondenceStatus(String muncipalityId, CorrespondenceStatus status);

	EmployeeChecklistEntity findByChecklistMunicipalityIdAndEmployeeUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistMunicipalityIdAndEmployeeManagerUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(String municipalityId, LocalDate date);

	Optional<EmployeeChecklistEntity> findByIdAndChecklistMunicipalityId(String id, String municipalityId);
}
