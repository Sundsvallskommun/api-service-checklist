package se.sundsvall.checklist.api.model;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingAndSortingBase;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OngoingEmployeeChecklistFilters extends AbstractParameterPagingAndSortingBase {
	private String employeeName;
	private String employeeUsername;
	private String managerName;
	private String delegatedTo;
	private String departmentName;
	private LocalDate employmentDate;
	private LocalDate purgeDate;
}
