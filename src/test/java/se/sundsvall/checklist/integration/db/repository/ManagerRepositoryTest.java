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
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.checklist.integration.db.model.ManagerEntity;

/**
 * ManagerRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class ManagerRepositoryTest {

	@Autowired
	private ManagerRepository repository;

	@Test
	void create() {
		// Arrange
		final var id = UUID.randomUUID();

		// Act
		final var result = repository.save(ManagerEntity.builder()
			.withPersonId(id.toString())
			.build());

		// Assert
		assertThat(result).isNotNull();
		assertThat(UUID.fromString(result.getPersonId())).isEqualTo(id);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

}
