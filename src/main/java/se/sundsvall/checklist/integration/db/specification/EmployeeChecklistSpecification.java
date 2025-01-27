package se.sundsvall.checklist.integration.db.specification;

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

	static Specification<EmployeeChecklistEntity> withNonCompletedChecklists() {
		return BUILDER.buildNotCompletedFilter();
	}

}
