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
@Schema(description = "Model for update request of fulfilment for a task")
public class EmployeeChecklistTaskUpdateRequest {

	@Schema(description = "The status of the task fulfilment", nullable = true, accessMode = WRITE_ONLY)
	private FulfilmentStatus fulfilmentStatus;

	@Schema(description = "The response text for the task fulfilment", examples = "Har bjudit på fika", nullable = true, accessMode = WRITE_ONLY)
	private String responseText;

	@Schema(description = "Identifier for the person that is performing the update", examples = "joe01doe", accessMode = WRITE_ONLY)
	@NotBlank
	private String updatedBy;
}
