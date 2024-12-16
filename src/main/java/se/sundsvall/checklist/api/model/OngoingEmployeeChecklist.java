package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
@Schema(description = "Model for ongoing employee checklist", accessMode = READ_ONLY)
public class OngoingEmployeeChecklist {

	@Schema(description = "The employee first name and last name concatenated", example = "John Doe")
	private String employeeName;

	@Schema(description = "The employee username", example = "johndoe")
	private String employeeUsername;

	@Schema(description = "The employees managers first name and last name concatenated ", example = "John Doe")
	private String managerName;

	@Schema(description = "The organization name", example = "Organization XYZ")
	private String organizationName;

	@Schema(description = "The names of the person(s) which have been delegated the checklist", example = "John Doe")
	private List<String> delegatedTo;

	@Schema(description = "The employment date of the employee", example = "2021-01-01")
	private LocalDate employmentDate;

	@Schema(description = "The purge date for the checklist", example = "2029-01-01")
	private LocalDate purgeDate;

}
