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
import se.sundsvall.checklist.integration.db.model.PhaseEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class PhaseRepositoryTest {

	@Autowired
	private PhaseRepository repository;

	@Test
	void save() {
		// Act
		final var result = repository.save(PhaseEntity.builder().withLastSavedBy("lastSavedBy").build());

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).hasSize(36);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void update() {
		// Act
		var result = repository.save(PhaseEntity.builder().withLastSavedBy("lastSavedBy").build());
		result.setName("modified");
		result = repository.saveAndFlush(result);

		// Assert
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void findAllByMunicipalityId() {
		// Act
		final var result = repository.findAllByMunicipalityId("2281");

		// Assert
		assertThat(result).hasSize(3).extracting(
			PhaseEntity::getId)
			.containsExactlyInAnyOrder(
				"1455a5d4-1db8-4a25-a49f-92fdd0c60a14",
				"2455a5d4-1db8-4a25-a49f-92fdd0c60a14",
				"3455a5d4-1db8-4a25-a49f-92fdd0c60a14");
	}

	@Test
	void findAllByMunicipalityIdNoMatch() {
		// Act and assert
		assertThat(repository.findAllByMunicipalityId("2282")).isEmpty();
	}

	@Test
	void findByIdAndMunicipalityId() {
		// Act and assert
		assertThat(repository.findByIdAndMunicipalityId("1455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2281")).isPresent();
		assertThat(repository.findByIdAndMunicipalityId("1455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2282")).isNotPresent();
		assertThat(repository.findByIdAndMunicipalityId("4455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2281")).isNotPresent();
	}

	@Test
	void existsByIdAndMunicipalityId() {
		// Act and assert
		assertThat(repository.existsByIdAndMunicipalityId("1455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2282")).isFalse();
		assertThat(repository.existsByIdAndMunicipalityId("2455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2281")).isTrue();
		assertThat(repository.existsByIdAndMunicipalityId("3455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2281")).isTrue();
		assertThat(repository.existsByIdAndMunicipalityId("4455a5d4-1db8-4a25-a49f-92fdd0c60a14", "2281")).isFalse();
	}
}
