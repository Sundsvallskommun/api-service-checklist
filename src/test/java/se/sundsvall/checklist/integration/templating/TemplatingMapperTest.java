package se.sundsvall.checklist.integration.templating;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.templating.model.EmployeeParameter;
import se.sundsvall.checklist.integration.templating.model.ManagerParameter;

class TemplatingMapperTest {

	@Test
	void mapChecklistEntityToRenderRequest() {

		// Arrange
		final var employeeFirstName = "someFirstName";
		final var employeeLastName = "someLastName";
		final var employeeStartDate = LocalDate.now();
		final var managerFirstName = "someFirstName";
		final var managerLastName = "someLastName";
		final var identifier = "someIdentifier";

		final var employee = EmployeeEntity.builder().withFirstName(employeeFirstName)
			.withLastName(employeeLastName)
			.withStartDate(employeeStartDate)
			.withManager(ManagerEntity.builder()
				.withFirstName(managerFirstName)
				.withLastName(managerLastName)
				.build())
			.build();

		// Act
		final var result = TemplatingMapper.toRenderRequest(employee, identifier);

		// Assert and verify
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(identifier);
		assertThat(result.getParameters()).hasSize(2);
		assertThat(result.getParameters().get("employee")).isNotNull().satisfies(employeeParameter -> {
			final var castedEmployeeParameter = (EmployeeParameter) employeeParameter;
			assertThat(castedEmployeeParameter.getFirstName()).isEqualTo(employeeFirstName);
			assertThat(castedEmployeeParameter.getLastName()).isEqualTo(employeeLastName);
			assertThat(castedEmployeeParameter.getStartDate()).isEqualTo(employeeStartDate);
		});
		assertThat(result.getParameters().get("manager")).isNotNull().satisfies(managerParameter -> {
			final var castedManagerParameter = (ManagerParameter) managerParameter;
			assertThat(castedManagerParameter.getFirstName()).isEqualTo(managerFirstName);
			assertThat(castedManagerParameter.getLastName()).isEqualTo(managerLastName);
		});
	}

}
