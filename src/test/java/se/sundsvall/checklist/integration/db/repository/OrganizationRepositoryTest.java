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
	void findOneByExistingOrganizationNumber() {
		// Arrange
		final var organizationNumber = 1;

		// Act
		final var result = repository.findOneByOrganizationNumber(organizationNumber);

		// Assert
		assertThat(result).isNotNull();
		assertThat(UUID.fromString(result.getId())).isNotNull();
		assertThat(result.getOrganizationNumber()).isEqualTo(organizationNumber);
	}

	@Test
	void findOneByNonExistingOrganizationNumber() {
		// Arrange
		final var organizationNumber = 999;

		// Act
		final var result = repository.findOneByOrganizationNumber(organizationNumber);

		// Assert
		assertThat(result).isNull();
	}
}
