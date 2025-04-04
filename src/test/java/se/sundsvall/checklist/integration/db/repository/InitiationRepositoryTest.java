package se.sundsvall.checklist.integration.db.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
class InitiationRepositoryTest {

	@Autowired
	private InitiationRepository repository;

	@Test
	void findAllByMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";

		// Act
		final var result = repository.findAllByMunicipalityId(municipalityId);

		// Assert
		assertThat(result).hasSize(2).satisfiesExactlyInAnyOrder(entity -> {
			assertThat(entity.getId()).isEqualTo("b6847217-3314-4686-a576-9c2344345ee5");
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(entity.getStatus()).isEqualTo("200 OK");
		}, entity -> {
			assertThat(entity.getId()).isEqualTo("ed71b4a2-3135-445d-b593-6060e9617181");
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(entity.getStatus()).isEqualTo("404 Not Found");
		});
	}

	@Test
	void findAllByNonExistingMunicipalityId() {
		// Arrange
		final var municipalityId = "2262";

		// Act and assert
		assertThat(repository.findAllByMunicipalityId(municipalityId)).isEmpty();
	}
}
