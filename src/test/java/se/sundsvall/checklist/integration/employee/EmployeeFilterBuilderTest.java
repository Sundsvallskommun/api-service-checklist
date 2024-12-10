package se.sundsvall.checklist.integration.employee;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmployeeFilterBuilderTest {

	@Test
	void testFilterCreatesValidString() {
		var wantedFilterString = "{\"HireDateFrom\":\"2020-01-01\",\"HireDateTo\":\"2022-01-01\",\"IsManual\":false,\"ShowOnlyNewEmployees\":true,\"PersonId\":\"2a22bae0-6c09-4bbd-a805-c2ccca22c7dd\"}";

		var filterString = new EmployeeFilterBuilder()
			.withShowOnlyNewEmployees(true)
			.withHireDateFrom(LocalDate.of(2020, 1, 1))
			.withHireDateTo(LocalDate.of(2022, 1, 1))
			.withPersonId("2a22bae0-6c09-4bbd-a805-c2ccca22c7dd")
			.withManual(false)
			.build();

		assertThat(filterString).isEqualTo(wantedFilterString);
	}

	@Test
	void testDefaultBuilder() {
		var wantedFilterString = "{\"HireDateFrom\":\"%s\"}".formatted(LocalDate.now().minusDays(30).format(ISO_DATE));

		var filterString = EmployeeFilterBuilder.buildDefaultNewEmployeeFilter();

		assertThat(filterString).isEqualTo(wantedFilterString);
	}

	@Test
	void testDefaultBuilderWithAddedFilter() {
		var wantedString = "{\"PersonId\":\"2a22bae0-6c09-4bbd-a805-c2ccca22c7dd\"}";

		var filterString = EmployeeFilterBuilder.getDefaultNewEmployeeFilterBuilder()
			.withPersonId("2a22bae0-6c09-4bbd-a805-c2ccca22c7dd")
			.build();

		assertThat(wantedString).isEqualTo(filterString);
	}

	@Test
	void testDefaultBuilder_shouldBeAbleToAddCompanyId() {
		var wantedString = "{\"CompanyId\":[2,3]}";

		var filterString = EmployeeFilterBuilder.getDefaultNewEmployeeFilterBuilder()
			.withCompanyId(2)
			.withCompanyId(3)
			.build();

		assertThat(wantedString).isEqualTo(filterString);
	}

	@Test
	void testDefaultBuilder_shouldBeAbleToAddCompanyIds() {
		var wantedString = "{\"CompanyId\":[1,2]}";

		var filterString = EmployeeFilterBuilder.getDefaultNewEmployeeFilterBuilder()
			.withCompanyIds(List.of(1, 2))
			.build();

		assertThat(wantedString).isEqualTo(filterString);
	}

	@Test
	void testDefaultBuilder_withOneAddedEventInfo() {
		var wantedString = "{\"EventInfo\":[\"Mover\"]}";

		var filterString = EmployeeFilterBuilder.getDefaultNewEmployeeFilterBuilder()
			.withEventInfo("Mover")
			.build();

		assertThat(wantedString).isEqualTo(filterString);
	}

	@Test
	void testDefaultBuilder_withTwoAddedEventInfos() {
		var wantedString = "{\"EventInfo\":[\"Mover\",\"Corporate\"]}";

		var filterString = EmployeeFilterBuilder.getDefaultNewEmployeeFilterBuilder()
			.withEventInfo("Mover", "Corporate")
			.build();

		assertThat(wantedString).isEqualTo(filterString);
	}

	@Test
	void testUuidEmployeeFilter() {
		var uuid = UUID.randomUUID().toString();
		var wantedFilterString = "{\"ShowOnlyNewEmployees\":false,\"PersonId\":\"%s\",\"EventInfo\":[\"Mover\",\"Corporate\",\"Company\",\"Rehire,Corporate\"]}".formatted(uuid);

		var filterString = EmployeeFilterBuilder.buildUuidEmployeeFilter(uuid);

		assertThat(filterString).isEqualTo(wantedFilterString);
	}
}
