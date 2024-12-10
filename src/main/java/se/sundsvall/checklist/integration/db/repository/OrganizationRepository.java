package se.sundsvall.checklist.integration.db.repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;

@CircuitBreaker(name = "organizationRepository")
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {
	Optional<OrganizationEntity> findByOrganizationNumberAndMunicipalityId(final int organizationNumber, final String municipalityId);

	Optional<OrganizationEntity> findByChecklistsIdAndChecklistsMunicipalityId(final String id, final String municipalityId);

	Optional<OrganizationEntity> findByIdAndMunicipalityId(final String id, final String municipalityId);

	List<OrganizationEntity> findAllByMunicipalityId(final String municipalityId);
}
