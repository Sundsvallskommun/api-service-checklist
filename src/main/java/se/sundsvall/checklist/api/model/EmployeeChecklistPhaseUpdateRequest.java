package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for update request on an employee checklist phase")
public class EmployeeChecklistPhaseUpdateRequest {

	@Schema(description = "The value to be set on all tasks in the phase", accessMode = WRITE_ONLY, nullable = true)
	private FulfilmentStatus tasksFulfilmentStatus;

	@Schema(description = "Identifier for the person that is performing the update", examples = "joe01doe", accessMode = WRITE_ONLY)
	@NotBlank
	private String updatedBy;
}
