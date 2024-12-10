package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;

@CircuitBreaker(name = "sortorderRepository")
public interface SortorderRepository extends JpaRepository<SortorderEntity, String> {
	List<SortorderEntity> findAllByMunicipalityIdAndOrganizationNumber(final String municipalityId, final int organizationNumber);

	List<SortorderEntity> findAllByComponentId(final String componentId);
}
