package se.sundsvall.checklist.integration.db.repository;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

/**
 * EmployeeChecklistRepository tests.
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
class EmployeeChecklistRepositoryTest {

	@Autowired
	private EmployeeChecklistRepository repository;

	@Test
	void create() {
		// Act
		final var result = repository.save(EmployeeChecklistEntity.builder().build());

		// Assert
		assertThat(result).isNotNull();
		assertThat(UUID.fromString(result.getId())).isNotNull();
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
		assertThat(result.isCompleted()).isFalse();
	}

	@Test
	void update() {
		// Act
		var result = repository.save(EmployeeChecklistEntity.builder().build());
		result.setLocked(true);
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void findByChecklistMunicipalityIdAndEmployeeUsername() {
		final var result = repository.findByChecklistsMunicipalityIdAndEmployeeUsername("2281", "aemp0loyee");

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("f853e2b1-a144-4305-b05e-ee8d6dc6d005");
	}

	@Test
	void findAllByChecklistMunicipalityIdAndEmployeeManagerUsername() {
		final var result = repository.findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername("2281", "aman0agr");

		assertThat(result)
			.hasSize(3)
			.extracting(EmployeeChecklistEntity::getId, EmployeeChecklistEntity::isCompleted)
			.containsExactlyInAnyOrder(
				tuple("f5960058-fad8-4825-85f3-b0fdb518adc5", true),
				tuple("223a076f-441d-4a30-b5d0-f2bfd5ab250b", false),
				tuple("f853e2b1-a144-4305-b05e-ee8d6dc6d005", true));
	}

	@Test
	void findAllByChecklistMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse() {
		assertThat(repository.findAllByChecklistsMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse("2281", LocalDate.of(2024, 10, 01)))
			.hasSize(1)
			.extracting(EmployeeChecklistEntity::getId).containsExactly(
				"f5960058-fad8-4825-85f3-b0fdb518adc5");

		assertThat(repository.findAllByChecklistsMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse("2281", LocalDate.of(2024, 10, 02)))
			.hasSize(2)
			.extracting(EmployeeChecklistEntity::getId).containsExactlyInAnyOrder(
				"f5960058-fad8-4825-85f3-b0fdb518adc5",
				"223a076f-441d-4a30-b5d0-f2bfd5ab250b");
	}

	@Test
	void findAllByChecklistMunicipalityIdAndCorrespondenceIsNull() {
		final var result = repository.findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull("2281");

		assertThat(result).hasSize(1)
			.extracting(EmployeeChecklistEntity::getId)
			.containsExactly("f5960058-fad8-4825-85f3-b0fdb518adc5");
	}

	@ParameterizedTest
	@EnumSource(value = CorrespondenceStatus.class)
	void findAllByChecklistMunicipalityIdAndCorrespondenceCorrespondenceStatus(CorrespondenceStatus status) {
		final var result = repository.findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus("2281", status);

		if (CorrespondenceStatus.SENT == status) {
			assertThat(result).hasSize(1);
			assertThat(result.getFirst().getId()).isEqualTo("f853e2b1-a144-4305-b05e-ee8d6dc6d005");
		} else if (CorrespondenceStatus.ERROR == status) {
			assertThat(result).hasSize(1);
			assertThat(result.getFirst().getId()).isEqualTo("223a076f-441d-4a30-b5d0-f2bfd5ab250b");
		} else {
			assertThat(result).isEmpty();
		}
	}

	@Test
	void findByIdAndChecklistMunicipalityId() {
		assertThat(repository.findByIdAndChecklistsMunicipalityId("223a076f-441d-4a30-b5d0-f2bfd5ab250b", "2281")).isPresent();
		assertThat(repository.findByIdAndChecklistsMunicipalityId("4bcdbe73-fff5-4f19-bb34-0c755423e473", "2281")).isEmpty();
		assertThat(repository.findByIdAndChecklistsMunicipalityId("223a076f-441d-4a30-b5d0-f2bfd5ab250b", "2262")).isEmpty();
	}

	@Test
	void findAllByOngoingEmployeeChecklistParameters() {
		final var result = repository.findAllByOngoingEmployeeChecklistParameters(new OngoingEmployeeChecklistParameters().withMunicipalityId("2281"), PageRequest.ofSize(100));

		assertThat(result).hasSize(1)
			.extracting(EmployeeChecklistEntity::getId)
			.containsExactly("223a076f-441d-4a30-b5d0-f2bfd5ab250b");
	}

	@Test
	void countByCorrespondenceCorrespondenceStatus() {
		assertThat(repository.countByCorrespondenceCorrespondenceStatus(ERROR)).isOne();
	}

	@Test
	void findAllByChecklistsMunicipalityIdAndCompletedFalse() {
		final var result = repository.findAllByChecklistsMunicipalityIdAndCompletedFalse("2281");

		assertThat(result).hasSize(1)
			.extracting(EmployeeChecklistEntity::getId)
			.containsExactly("223a076f-441d-4a30-b5d0-f2bfd5ab250b");
	}
}
