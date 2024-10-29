package se.sundsvall.checklist.integration.db.repository;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.integration.db.model.OrganizationEntity;

/**
 * EmployeeRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class OrganizationRepositoryTest {

	@Autowired
	private OrganizationRepository repository;

	@Test
	void create() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "OrganizationName";

		final var entity = OrganizationEntity.builder()
			.withOrganizationName(organizationName)
			.withOrganizationNumber(organizationNumber)
			.build();

		// Act
		final var result = repository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(UUID.fromString(result.getId())).isNotNull();
		assertThat(result.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(result.getOrganizationName()).isEqualTo(organizationName);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void update() {
		// Act
		final var organizationNumber = 123;
		final var organizationName = "OrganizationName";

		final var entity = OrganizationEntity.builder()
			.withOrganizationName(organizationName)
			.withOrganizationNumber(organizationNumber)
			.build();

		var result = repository.save(entity);
		result.setOrganizationName("modified");
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void findByOrganizationNumberAndMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";
		final var organizationNumber = 1;

		// Act
		final var result = repository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId);

		// Assert
		assertThat(result).isPresent().hasValueSatisfying(entity -> {
			assertThat(UUID.fromString(entity.getId())).isNotNull();
			assertThat(entity.getOrganizationNumber()).isEqualTo(organizationNumber);
		});
	}

	@Test
	void findByNonExistingOrganizationNumberAndMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";
		final var organizationNumber = 999;

		// Act and assert
		assertThat(repository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId)).isEmpty();
	}

	@Test
	void findByChecklistsIdAndChecklistsMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";
		final var checklistId = "35764278-50c8-4a19-af00-077bfc314fd2";

		// Act
		final var result = repository.findByChecklistsIdAndChecklistsMunicipalityId(checklistId, municipalityId);

		// Assert
		assertThat(result).isPresent().hasValueSatisfying(entity -> {
			assertThat(UUID.fromString(entity.getId())).isNotNull();
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(entity.getChecklists()).hasSize(3).satisfiesOnlyOnce(checklist -> {
				assertThat(checklist.getId()).isEqualTo(checklistId);
				assertThat(checklist.getName()).isEqualTo("Cheflista");
			});
		});
	}

	@Test
	void findByNonExistingChecklistsIdAndChecklistsMunicipalityId() {
		// Arrange
		final var municipalityId = "2262";
		final var checklistId = "35764278-50c8-4a19-af00-077bfc314fd2";

		// Act and assert
		assertThat(repository.findByChecklistsIdAndChecklistsMunicipalityId(checklistId, municipalityId)).isEmpty();
	}

	@Test
	void findByIdAndMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";
		final var id = "bd49f474-303c-4a4e-aa54-5d4f58d9188b";

		// Act
		final var result = repository.findByIdAndMunicipalityId(id, municipalityId);

		// Assert
		assertThat(result).isPresent().hasValueSatisfying(entity -> {
			assertThat(entity.getId()).isEqualTo(id);
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		});
	}

	@Test
	void findByNonExistingIdAndMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";
		final var id = "00d4a6b7-ba3f-461a-ae97-cb32b539b7af";

		// Act and assert
		assertThat(repository.findByIdAndMunicipalityId(id, municipalityId)).isEmpty();
	}

	@Test
	void findAllByMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";

		// Act
		final var result = repository.findAllByMunicipalityId(municipalityId);

		// Assert
		assertThat(result).hasSize(2).satisfiesExactlyInAnyOrder(entity -> {
			assertThat(entity.getOrganizationNumber()).isEqualTo(1);
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		}, entity -> {
			assertThat(entity.getOrganizationNumber()).isEqualTo(5535);
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		});
	}

	@Test
	void findAllByNonExistingMunicipalityId() {
		// Arrange
		final var municipalityId = "2262";

		// Act and assert
		assertThat(repository.findAllByMunicipalityId(municipalityId)).isEmpty();
	}
}
