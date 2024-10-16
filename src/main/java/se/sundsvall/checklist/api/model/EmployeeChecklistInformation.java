package se.sundsvall.checklist.api.model;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Basic information about an employee checklist")
public class EmployeeChecklistInformation {

	@Schema(description = "The id of the employee checklist", example = "e2cb57a7-7a9d-4b9b-993d-022c75fc5cd8", accessMode = Schema.AccessMode.READ_ONLY)
	private String id;

	@Schema(description = "The name of the employee", example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
	private String employeeName;

	@Schema(description = "The username of the employee", example = "johndoe", accessMode = Schema.AccessMode.READ_ONLY)
	private String employeeUsername;

	@Schema(description = "The organization name", example = "Fictional Municipality", accessMode = Schema.AccessMode.READ_ONLY)
	private String organizationName;

	@Schema(description = "The name of the manager", example = "Maria Smith", accessMode = Schema.AccessMode.READ_ONLY)
	private String managerName;

	@Schema(description = "The delegated managers name", example = "Luke Hamilton", accessMode = Schema.AccessMode.READ_ONLY)
	private List<String> delegatedTo;

	@Schema(description = "The start date for the employee", example = "2021-01-01", accessMode = Schema.AccessMode.READ_ONLY)
	private LocalDate employeeStartDate;

}
