package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.ArrayList;
import java.util.List;

import org.zalando.problem.StatusType;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Schema(description = "Model for employee checklist triggering response")
public class EmployeeChecklistResponse {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Builder(setterPrefix = "with")
	@Schema(description = "Model for employee checklist triggering detailed status")
	public static class Detail {

		@Schema(description = "Status for action of creating the employee checklist", accessMode = READ_ONLY)
		private StatusType status;

		@Schema(description = "Descriptive text for of the creation outcome", example = "Employee with loginname abc123 processed successfully.", accessMode = READ_ONLY)
		private String information;
	}

	@Schema(description = "Summary for execution", example = "Successful execution", accessMode = READ_ONLY)
	private String summary;

	@ArraySchema(arraySchema = @Schema(implementation = Detail.class, description = "Details for each user specific creation", accessMode = READ_ONLY))
	@Builder.Default
	private List<Detail> details = new ArrayList<>();
}
