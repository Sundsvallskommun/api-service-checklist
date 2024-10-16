package se.sundsvall.checklist.integration.db.repository;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class ChecklistRepositoryTest {

	@Autowired
	private ChecklistRepository checklistRepository;

	@Test
	void saveTest() {
		final var result = checklistRepository.save(ChecklistEntity.builder().build());

		assertThat(result).isNotNull();
		assertThat(result.getId()).hasSize(36);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();

		final var persistedEntity = checklistRepository.findById(result.getId());
		assertThat(persistedEntity).contains(result);
	}

	@Test
	void updateTest() {
		final var result = checklistRepository.save(ChecklistEntity.builder().build());
		result.setName("updated");
		checklistRepository.saveAndFlush(result);

		assertThat(checklistRepository.findById(result.getId())).isPresent().hasValueSatisfying(updatedBean -> {
			assertThat(updatedBean).usingRecursiveAssertion().isEqualTo(result);
			assertThat(updatedBean.getUpdated()).isCloseTo(now(), within(2, SECONDS));
		});
	}

	@Test
	void deleteByIdTest() {
		final var id = "35764278-50c8-4a19-af00-077bfc314fd2";
		final var before = checklistRepository.findById(id);
		assertThat(before).isNotEmpty();

		checklistRepository.deleteById(id);

		final var result = checklistRepository.findById(id);
		assertThat(result).isEmpty();
	}

	@Test
	void findByIdTest() {
		final var result = checklistRepository.findById("15764278-50c8-4a19-af00-077bfc314fd2");

		assertThat(result).isNotEmpty().satisfies(r -> {
			assertThat(r.get().getId()).isEqualTo("15764278-50c8-4a19-af00-077bfc314fd2");
			assertThat(r.get().getName()).isEqualTo("Checklista Elnät");
			assertThat(r.get().getVersion()).isEqualTo(1);
			assertThat(r.get().getRoleType()).isEqualTo(EMPLOYEE);
		});
	}

	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata-junit.sql"
	})
	void findAllTest() {
		final var result = checklistRepository.findAll();

		assertThat(result).isNotEmpty().hasSize(3);
	}

	@Test
	void existsByNameTest() {
		final var result = checklistRepository.existsByName("Checklista Elnät");

		assertThat(result).isTrue();
	}

	@Test
	void existsByNameAndLifeCycleTest() {
		final var result = checklistRepository.existsByNameAndLifeCycle("Checklista för Vård och omsorg", ACTIVE);

		assertThat(result).isTrue();
	}

	@Test
	void findByNameAndLifeCycleTest() {
		final var result = checklistRepository.findByNameAndLifeCycle("Checklista för Vård och omsorg", ACTIVE);

		assertThat(result).isNotEmpty().satisfies(r -> {
			assertThat(r.get().getId()).isEqualTo("25764278-50c8-4a19-af00-077bfc314fd2");
			assertThat(r.get().getName()).isEqualTo("Checklista för Vård och omsorg");
			assertThat(r.get().getVersion()).isEqualTo(1);
			assertThat(r.get().getRoleType()).isEqualTo(EMPLOYEE);
		});
	}

}
