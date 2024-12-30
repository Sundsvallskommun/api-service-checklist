package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
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

	public static PagingMetaData toPagingMetaData(Page<?> page1) {
		return ofNullable(page1)
			.map(page -> PagingMetaData.create()
				.withPage(page.getNumber())
				.withLimit(page.getSize())
				.withCount(page.getNumberOfElements())
				.withTotalRecords(page.getTotalElements())
				.withTotalPages(page.getTotalPages()))
			.orElse(null);
	}

	public static PageRequest toPageRequest(final OngoingEmployeeChecklistParameters parameters) {
		if (parameters == null) {
			return null;
		}

		var sortBy = Optional.ofNullable(parameters.getSortBy()).orElse(emptyList()).stream()
			.map(apiToAttributeMapping::get)
			.flatMap(List::stream)
			.toList();

		if (!sortBy.isEmpty()) {
			parameters.setSortBy(sortBy);
		}

		return PageRequest.of(parameters.getPage() - 1, parameters.getLimit(), parameters.sort());
	}
}
