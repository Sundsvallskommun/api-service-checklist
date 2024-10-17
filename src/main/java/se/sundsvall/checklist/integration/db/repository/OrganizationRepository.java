package se.sundsvall.checklist.integration.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;

@CircuitBreaker(name = "organizationRepository")
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {
	OrganizationEntity findOneByOrganizationNumber(int organizationNumber);

	Optional<OrganizationEntity> findByOrganizationNumber(int organizationNumber);

	Optional<OrganizationEntity> findByChecklistsId(String id);
}
