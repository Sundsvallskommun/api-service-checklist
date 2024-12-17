package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;
import se.sundsvall.checklist.integration.db.repository.projection.OngoingEmployeeChecklistProjection;

@CircuitBreaker(name = "employeeChecklistRepository")
public interface EmployeeChecklistRepository extends JpaRepository<EmployeeChecklistEntity, String>, PagingAndSortingRepository<EmployeeChecklistEntity, String>, JpaSpecificationExecutor<EmployeeChecklistEntity> {
	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(String municipalityId);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(String municipalityId, CorrespondenceStatus status);

	EmployeeChecklistEntity findByChecklistsMunicipalityIdAndEmployeeUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(String municipalityId, LocalDate date);

	Optional<EmployeeChecklistEntity> findByIdAndChecklistsMunicipalityId(String id, String municipalityId);

	// Page<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndStartDateIsBeforeAndEndDateIsAfter(String
	// municipalityId, LocalDate startDate, LocalDate endDate, Pageable pageable);

	@Query("SELECT "
		+ "(e.employee.firstName || ' ' || e.employee.lastName) AS employeeName,"
		+ "(e.employee.username) as employeeUsername,"
		+ "(e.employee.department.organizationName) as departmentName,"
		+ "(e.employee.manager.firstName || ' ' || e.employee.manager.lastName) as managerName,"
		+ "(e.startDate) as employmentDate,"
		+ "(e.endDate) as purgeDate "
		+ "FROM EmployeeChecklistEntity e "
		+ "WHERE e.employee.company.municipalityId = :municipalityId "
		+ "AND e.startDate <= :startDate "
		+ "AND e.endDate >= :endDate")
	Page<OngoingEmployeeChecklistProjection> findAllByChecklistsMunicipalityIdAndStartDateIsBeforeAndEndDateIsAfter(String municipalityId, LocalDate startDate, LocalDate endDate, Pageable pageable);

}
