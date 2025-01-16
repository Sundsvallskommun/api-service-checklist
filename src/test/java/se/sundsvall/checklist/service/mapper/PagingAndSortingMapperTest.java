package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;

class PagingAndSortingMapperTest {

	@Test
	void toPagingMetaData() {
		final var page = Mockito.mock(Page.class);
		when(page.getNumber()).thenReturn(1);
		when(page.getSize()).thenReturn(25);
		when(page.getNumberOfElements()).thenReturn(25);
		when(page.getTotalElements()).thenReturn(1200L);
		when(page.getTotalPages()).thenReturn(48);

		final var pagingMetaData = PagingAndSortingMapper.toPagingMetaData(page);

		assertThat(pagingMetaData).isNotNull().satisfies(meta -> {
			assertThat(meta.getPage()).isEqualTo(2);
			assertThat(meta.getLimit()).isEqualTo(25);
			assertThat(meta.getCount()).isEqualTo(25);
			assertThat(meta.getTotalRecords()).isEqualTo(1200L);
			assertThat(meta.getTotalPages()).isEqualTo(48);
		});
	}

	@Test
	void toPageRequestFromNull() {
		assertThat(PagingAndSortingMapper.toPageRequest(null)).isNull();
	}

	@ParameterizedTest
	@MethodSource(value = "toPageRequestArgumentProvider")
	void toPageRequest(String sortBy, List<String> expected) {
		final var parameters = new OngoingEmployeeChecklistParameters();
		parameters.setPage(5);
		parameters.setLimit(25);
		parameters.setSortBy(List.of(sortBy));

		final var pageRequest = PagingAndSortingMapper.toPageRequest(parameters);

		assertThat(pageRequest).isNotNull().satisfies(request -> {
			assertThat(request.getPageNumber()).isEqualTo(parameters.getPage() - 1);
			assertThat(request.getPageSize()).isEqualTo(parameters.getLimit());
			request.getSort().forEach(order -> assertThat(order.getProperty()).isIn(expected));
		});

	}

	private static Stream<Arguments> toPageRequestArgumentProvider() {
		return Stream.of(
			Arguments.of("nonTranslatedValue", List.of("nonTranslatedValue")),
			Arguments.of("employeeName", List.of("employee_firstName", "employee_lastName")),
			Arguments.of("managerName", List.of("employee_manager_firstName", "employee_manager_lastName")),
			Arguments.of("departmentName", List.of("employee_department_organizationName")),
			Arguments.of("employmentDate", List.of("startDate")),
			Arguments.of("purgeDate", List.of("endDate")),
			Arguments.of("delegatedTo", List.of("delegates")));
	}
}
