package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.generatePortalPersonData;
import static se.sundsvall.checklist.service.mapper.DelegateMapper.getUserNameFromLoginName;
import static se.sundsvall.checklist.service.mapper.DelegateMapper.toDelegateEntity;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

class DelegateMapperTest {

	@Test
	void mapPortalPersonDataToDelegateEntityTest() {
		final var portalPersonData = generatePortalPersonData(UUID.randomUUID());
		final var employeeChecklist = createEmployeeChecklistEntity();

		final var result = toDelegateEntity(portalPersonData, employeeChecklist);

		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.getDelegatedBy()).isEqualTo(employeeChecklist.getEmployee().getManager());
			assertThat(r.getEmail()).isEqualTo(portalPersonData.getEmail());
			assertThat(r.getUserName()).isEqualTo("tes10tes");
			assertThat(r.getPartyId()).isEqualTo(portalPersonData.getPersonid().toString());
			assertThat(r.getEmployeeChecklist()).isEqualTo(employeeChecklist);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = { "MEDBORGARE\\abc10def", "PERSONAL\\abc10def", "TEST\\abc10def" })
	void getUserNameFromLoginNameTest(final String value) {

		final var result = getUserNameFromLoginName(value);

		assertThat(result).isEqualTo("abc10def");
	}

	@Test
	void getUserNameFromLoginNameInvalidTest() {
		assertThatThrownBy(() -> getUserNameFromLoginName("abc10def"))
			.isInstanceOf(Problem.class);
	}

}
