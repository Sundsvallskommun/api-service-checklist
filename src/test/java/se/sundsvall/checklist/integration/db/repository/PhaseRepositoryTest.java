package se.sundsvall.checklist.integration.db.repository;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.checklist.integration.db.model.PhaseEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
class PhaseRepositoryTest {

	@Autowired
	private PhaseRepository repository;

	@Test
	void saveTest() {
		// Act
		final var result = repository.save(PhaseEntity.builder().withLastSavedBy("lastSavedBy").build());

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).hasSize(36);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void updateTest() {
		// Act
		var result = repository.save(PhaseEntity.builder().withLastSavedBy("lastSavedBy").build());
		result.setName("modified");
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}
}
