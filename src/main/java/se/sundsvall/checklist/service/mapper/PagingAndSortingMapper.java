package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

public final class PagingAndSortingMapper {

	private static final Map<String, List<String>> apiToAttributeMapping = Map.of(
		"employeeName", List.of("employee_firstName", "employee_lastName"),
		"managerName", List.of("employee_manager_firstName", "employee_manager_lastName"),
		"departmentName", List.of("employee_department_organizationName"),
		"employmentDate", List.of("startDate"),
		"purgeDate", List.of("endDate"),
		"delegatedTo", List.of("delegates"));

	private PagingAndSortingMapper() {}

	public static PagingMetaData toPagingMetaData(Page<?> page) {
		return ofNullable(page)
			.map(p -> PagingMetaData.create()
				.withPage(p.getNumber() + 1) // page number is zero based and needs a + 1
				.withLimit(p.getSize())
				.withCount(p.getNumberOfElements())
				.withTotalRecords(p.getTotalElements())
				.withTotalPages(p.getTotalPages()))
			.orElse(null);
	}

	public static PageRequest toPageRequest(final OngoingEmployeeChecklistParameters parameters) {
		if (parameters == null) {
			return null;
		}

		final var sortBy = Optional.ofNullable(parameters.getSortBy()).orElse(emptyList()).stream()
			.map(apiToAttributeMapping::get)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.toList();

		if (!sortBy.isEmpty()) {
			parameters.setSortBy(sortBy);
		}

		return PageRequest.of(parameters.getPage() - 1, parameters.getLimit(), parameters.sort());
	}
}
