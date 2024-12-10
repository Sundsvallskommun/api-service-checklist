package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.checklist.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ChecklistPropertiesTest {

	@Autowired
	private ChecklistProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.managedMunicipalityIds()).containsExactlyInAnyOrder("value_1", "value_2");
	}
}
