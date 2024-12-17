package se.sundsvall.checklist.integration.db.repository.projection;

import java.time.LocalDate;
import java.util.List;

public interface OngoingEmployeeChecklistProjection {

	String getEmployeeName();

	String getEmployeeUsername();

	String getDepartmentName();

	String getManagerName();

	List<String> getDelegatedTo();

	LocalDate getEmploymentDate();

	LocalDate getPurgeDate();

}
