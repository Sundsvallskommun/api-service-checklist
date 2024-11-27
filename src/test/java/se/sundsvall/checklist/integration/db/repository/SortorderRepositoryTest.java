package se.sundsvall.checklist.integration.db.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;

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
	void existsByMunicipalityIdAndOrganizationNumber() {
		// Act and assert
		assertThat(repository.existsByMunicipalityIdAndOrganizationNumber("2281", 578)).isTrue();
		assertThat(repository.existsByMunicipalityIdAndOrganizationNumber("2281", 579)).isFalse();
		assertThat(repository.existsByMunicipalityIdAndOrganizationNumber("2282", 578)).isFalse();
	}

	@Test
	void deleteAllByMunicipalityIdAndOrganizationNumber() {
		// Assert posts exist
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 13)).hasSize(6);

		// Act
		repository.deleteAllByMunicipalityIdAndOrganizationNumber("2281", 13);

		// Assert
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 13)).isEmpty();
	}

	@Test
	void delete() {
		// Assert post exist
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 14)).hasSize(1);

		// Act
		repository.deleteByMunicipalityIdAndOrganizationNumberAndComponentId("2281", 14, "7121d85d-6eee-49b4-8f1d-db1e165a5c29");

		// Assert
		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", 14)).isEmpty();
	}
}
