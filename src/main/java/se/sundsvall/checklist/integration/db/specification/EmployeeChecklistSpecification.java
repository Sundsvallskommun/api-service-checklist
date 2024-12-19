package se.sundsvall.checklist.integration.db.specification;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

public interface EmployeeChecklistSpecification {

	SpecificationBuilder<EmployeeChecklistEntity> BUILDER = new SpecificationBuilder<>();

	static Specification<EmployeeChecklistEntity> distinct() {
		return BUILDER.distinct();
	}

	static Specification<EmployeeChecklistEntity> withMunicipalityId(final String municipalityId) {
		return BUILDER.buildMunicipalityIdFilter(municipalityId);
	}

	static Specification<EmployeeChecklistEntity> withEmployeeName(final String employeeName) {
		return BUILDER.buildEmployeeNameFilter(employeeName);
	}

	static Specification<EmployeeChecklistEntity> withStartDateEqualOrBefore(final LocalDate startDate) {
		return BUILDER.buildStartDateEqualOrBeforeFilter("startDate", startDate);
	}

	static Specification<EmployeeChecklistEntity> withEndDateEqualOrAfter(final LocalDate endDate) {
		return BUILDER.buildEndDateAfterFilter("endDate", endDate);
	}

}
