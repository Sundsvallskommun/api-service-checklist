package se.sundsvall.checklist.integration.db.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class CustomTaskRepositoryTest {

	@Autowired
	private CustomTaskRepository repository;

	@Test
	void findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistMunicipalityIdTest() {
		assertThat(repository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId("f853e2b1-a144-4305-b05e-ee8d6dc6d005", "2281")).hasSize(1).satisfiesExactly(customTask -> {
			assertThat(customTask.getId()).isEqualTo("1b3bfe66-0e6c-4e92-a410-7c620a5461f4");
			assertThat(customTask.getPhase().getId()).isEqualTo("2455a5d4-1db8-4a25-a49f-92fdd0c60a14");
			assertThat(customTask.getEmployeeChecklist().getId()).isEqualTo("f853e2b1-a144-4305-b05e-ee8d6dc6d005");
		});
	}

	@Test
	void countByPhaseIdTest() {
		assertThat(repository.countByPhaseId("1455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isZero();
		assertThat(repository.countByPhaseId("2455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isOne();
		assertThat(repository.countByPhaseId("3455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isZero();
	}
}
