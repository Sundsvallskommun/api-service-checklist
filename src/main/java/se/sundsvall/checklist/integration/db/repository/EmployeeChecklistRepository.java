package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
import se.sundsvall.checklist.integration.db.model.ChecklistEmployee;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

import static se.sundsvall.checklist.integration.db.specification.EmployeeChecklistSpecification.distinct;
import static se.sundsvall.checklist.integration.db.specification.EmployeeChecklistSpecification.withEmployeeName;
import static se.sundsvall.checklist.integration.db.specification.EmployeeChecklistSpecification.withMunicipalityId;
import static se.sundsvall.checklist.integration.db.specification.EmployeeChecklistSpecification.withNonCompletedChecklists;

@Repository
@CircuitBreaker(name = "employeeChecklistRepository")
public interface EmployeeChecklistRepository extends JpaRepository<EmployeeChecklistEntity, String>, PagingAndSortingRepository<EmployeeChecklistEntity, String>, JpaSpecificationExecutor<EmployeeChecklistEntity> {
	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(String municipalityId);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(String municipalityId, CorrespondenceStatus status);

	@Query("""
		select distinct new se.sundsvall.checklist.integration.db.model.ChecklistEmployee(
		employee.id, employee.firstName, employee.lastName, employee.username)
		from EmployeeChecklistEntity employeeChecklist
		join employeeChecklist.employee employee
		join employeeChecklist.checklists checklist
		where checklist.municipalityId = :municipalityId
		and employeeChecklist.completed = false
		""")
	List<ChecklistEmployee> findOngoingChecklistEmployees(@Param("municipalityId") String municipalityId);

	@Query("""
		select distinct new se.sundsvall.checklist.integration.db.model.ChecklistEmployee(
		employee.id, employee.firstName, employee.lastName, employee.username)
		from EmployeeChecklistEntity employeeChecklist
		join employeeChecklist.employee employee
		join employeeChecklist.checklists checklist
		where checklist.municipalityId = :municipalityId
		and employee.username = :username
		""")
	Optional<ChecklistEmployee> findChecklistEmployee(@Param("municipalityId") String municipalityId, @Param("username") String username);

	int countByCorrespondenceCorrespondenceStatus(CorrespondenceStatus status);

	EmployeeChecklistEntity findByChecklistsMunicipalityIdAndEmployeeUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername(String municipalityId, String username);

	List<EmployeeChecklistEntity> findAllByChecklistsMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(String municipalityId, LocalDate date);

	Optional<EmployeeChecklistEntity> findByIdAndChecklistsMunicipalityId(String id, String municipalityId);

	List<EmployeeChecklistEntity> findAllByChecklistsTasksId(String taskId);

	default Page<EmployeeChecklistEntity> findAllByOngoingEmployeeChecklistParameters(final OngoingEmployeeChecklistParameters parameters, final Pageable pageable) {
		return findAll(Specification
			.allOf(withMunicipalityId(parameters.getMunicipalityId())
				.and(withEmployeeName(parameters.getEmployeeName()))
				.and(withNonCompletedChecklists())
				.and(distinct())),
			pageable);
	}

}
