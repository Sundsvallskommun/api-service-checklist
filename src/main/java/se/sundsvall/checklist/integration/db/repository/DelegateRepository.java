package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

@Repository
@CircuitBreaker(name = "delegateRepository")
public interface DelegateRepository extends JpaRepository<DelegateEntity, String> {

	Optional<DelegateEntity> findByEmployeeChecklistAndEmail(final EmployeeChecklistEntity employeeChecklist, final String email);

	List<DelegateEntity> findAllByEmployeeChecklistId(final String employeeChecklistId);

	List<DelegateEntity> findAllByUsername(final String username);

	boolean existsByEmployeeChecklistAndEmail(final EmployeeChecklistEntity employeeChecklist, final String email);

	void deleteByEmployeeChecklistAndEmail(final EmployeeChecklistEntity employeeChecklist, final String email);

	void deleteByEmployeeChecklist(final EmployeeChecklistEntity employeeChecklist);
}
