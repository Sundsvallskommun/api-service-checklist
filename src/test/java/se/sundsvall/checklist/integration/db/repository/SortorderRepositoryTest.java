package se.sundsvall.checklist.integration.db.repository;

import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class SortorderRepositoryTest {

	@Autowired
	private SortorderRepository repository;

	@Test
	void save() {
		// Arrange
		final var entity = SortorderEntity.builder()
			.withComponentId(UUID.randomUUID().toString())
			.withComponentType(ComponentType.PHASE)
			.withMunicipalityId("2262")
			.withOrganizationNumber(RandomUtils.secure().randomInt())
			.withPosition(RandomUtils.secure().randomInt())
			.build();

		// Act
		final var result = repository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertDoesNotThrow(() -> UUID.fromString(result.getId()));
	}

	@Test
	void findAllByMunicipalityIdAndOrganizationNumber() {
		// Act
		final var result = repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 578);

		// Assert
		assertThat(result).hasSize(7);
		assertThat(result.stream().filter(e -> ComponentType.PHASE == e.getComponentType()).toList()).hasSize(2)
			.extracting(SortorderEntity::getId)
			.containsExactlyInAnyOrder(
				"07ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"08ca2228-c49e-4e36-91c6-8e3bcb733c14");
		assertThat(result.stream().filter(e -> ComponentType.TASK == e.getComponentType()).toList()).hasSize(5)
			.extracting(SortorderEntity::getId)
			.containsExactlyInAnyOrder(
				"09ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"10ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"11ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"12ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"13ca2228-c49e-4e36-91c6-8e3bcb733c14");
	}

	@Test
	void findAllByMunicipalityIdAndOrganizationNumberNoMatch() {
		// Act and assert
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2282", 578)).isEmpty();
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 579)).isEmpty();
	}

	@Test
	void findAllByComponentId() {
		// Act
		final var result = repository.findAllByComponentId("7121d85d-6eee-49b4-8f1d-db1e165a5c29");

		// Assert
		assertThat(result).hasSize(3);
		assertThat(result.stream())
			.extracting(SortorderEntity::getId)
			.containsExactlyInAnyOrder(
				"06ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"14ca2228-c49e-4e36-91c6-8e3bcb733c14",
				"13ca2228-c49e-4e36-91c6-8e3bcb733c14");
	}

	@Test
	void findAllByComponentIdNoMatch() {
		assertThat(repository.findAllByComponentId("06ca2228-c49e-4e36-91c6-8e3bcb733c14")).isEmpty();
	}
}
