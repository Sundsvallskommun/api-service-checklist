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
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.integration.db.model.TaskEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class TaskRepositoryTest {

	@Autowired
	private TaskRepository repository;

	@Test
	void saveTest() {
		final var result = repository.save(TaskEntity.builder().withLastSavedBy("lastSavedBy").build());
		assertThat(result).isNotNull();
		assertThat(result.getId()).hasSize(36);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void updateTest() {
		// Act
		var result = repository.save(TaskEntity.builder().withLastSavedBy("lastSavedBy").build());
		result.setText("modified");
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void deleteTest() {
		final var entityId = "aba82aca-f841-4257-baec-d745e3ab78bf";
		final var entity = repository.findById(entityId);

		assertThat(entity).isNotEmpty();

		repository.delete(entity.get());

		assertThat(repository.findById(entityId)).isEmpty();
	}

	@Test
	void countByPhaseIdTest() {
		assertThat(repository.countByPhaseId("1455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isEqualTo(3);
		assertThat(repository.countByPhaseId("2455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isZero();
		assertThat(repository.countByPhaseId("3455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isZero();
	}

}
