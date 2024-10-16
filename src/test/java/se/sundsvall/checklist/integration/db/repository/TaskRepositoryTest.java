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
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.checklist.integration.db.model.TaskEntity;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
class TaskRepositoryTest {

	@Autowired
	private TaskRepository taskRepository;

	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata-junit.sql"
	})
	@Test
	void saveTest() {
		final var result = taskRepository.save(TaskEntity.builder()
			.build());
		assertThat(result).isNotNull();
		assertThat(result.getId()).hasSize(36);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata-junit.sql"
	})
	@Test
	void deleteTest() {
		final var entity = taskRepository.findById("aba82aca-f841-4257-baec-d745e3ab78bf");
		assertThat(entity).isNotEmpty();

		taskRepository.delete(entity.get());

		final var deletedEntity = taskRepository.findById("aba82aca-f841-4257-baec-d745e3ab78bf");
		assertThat(deletedEntity).isEmpty();
	}
}
