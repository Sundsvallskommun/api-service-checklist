package se.sundsvall.checklist.integration.db.repository;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

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
class EmployeeRepositoryTest {

	@Autowired
	private EmployeeRepository repository;

	@Test
	void create() {
		// Arrange
		final var id = UUID.randomUUID();

		// Act
		final var result = repository.save(EmployeeEntity.builder()
			.withId(id.toString())
			.build());

		// Assert
		assertThat(result).isNotNull();
		assertThat(UUID.fromString(result.getId())).isEqualTo(id);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void update() {
		// Arrange
		final var id = UUID.randomUUID();

		// Act
		var result = repository.save(EmployeeEntity.builder().withId(id.toString()).build());
		result.setLastName("modified");
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

}
