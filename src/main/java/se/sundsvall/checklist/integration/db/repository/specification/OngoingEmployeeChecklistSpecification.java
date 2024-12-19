package se.sundsvall.checklist.integration.db.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

public interface OngoingEmployeeChecklistSpecification {
	final String EMPLOYEE = "employee";
	final String FULL_NAME = "fullName";
	final String USERNAME = "username";
	final String MANAGER = "manager";
	final String DEPARTMENT = "department";
	final String ORGANIZATION_NAME = "organizationName";
	final String DELEGATES = "delegates";

	SpecificationBuilder<EmployeeChecklistEntity> BUILDER = new SpecificationBuilder<>();

	static Specification<EmployeeChecklistEntity> withEmployeeName(String value) {
		return BUILDER.buildLikeFilter(EMPLOYEE, FULL_NAME, value);
	}

	static Specification<EmployeeChecklistEntity> withEmployeeUsername(String value) {
		return BUILDER.buildLikeFilter(EMPLOYEE, USERNAME, value);
	}

	static Specification<EmployeeChecklistEntity> withManagerName(String value) {
		return BUILDER.buildLikeFilter(EMPLOYEE, MANAGER, FULL_NAME, value);
	}

	static Specification<EmployeeChecklistEntity> withDepartmentName(String value) {
		return BUILDER.buildLikeFilter(EMPLOYEE, DEPARTMENT, ORGANIZATION_NAME, value);
	}

	static Specification<EmployeeChecklistEntity> withDelegatedToName(String value) {
		return BUILDER.buildLikeFilter(DELEGATES, FULL_NAME, value);
	}
}
