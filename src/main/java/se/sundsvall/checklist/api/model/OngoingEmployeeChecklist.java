package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Model for summarized information for an ongoing employee checklist", accessMode = READ_ONLY)
public class OngoingEmployeeChecklist {

	@Schema(description = "The employee first name and last name concatenated", examples = "John Doe")
	private String employeeName;

	@Schema(description = "The employee username", examples = "johndoe")
	private String employeeUsername;

	@Schema(description = "The employees managers first name and last name concatenated ", examples = "John Doe")
	private String managerName;

	@Schema(description = "The organization name", examples = "Organization XYZ")
	private String departmentName;

	@Schema(description = "The names of the person(s) which have been delegated the checklist", examples = "John Doe")
	private List<String> delegatedTo;

	@Schema(description = "The employment date of the employee", examples = "2021-01-01")
	private LocalDate employmentDate;

	@Schema(description = "The purge date for the checklist", examples = "2029-01-01")
	private LocalDate purgeDate;

}
