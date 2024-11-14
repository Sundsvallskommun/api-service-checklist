package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.api.validation.ValidPeriod;
import se.sundsvall.checklist.integration.db.model.enums.Permission;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for phase update request")
public class PhaseUpdateRequest {

	@Schema(description = "The name of the phase", example = "Första veckan", accessMode = WRITE_ONLY)
	private String name;

	@Schema(description = "The body text of the phase", example = "Detta är en beskrivning av vad som ska göras under första veckan", accessMode = WRITE_ONLY)
	private String bodyText;

	@Schema(description = "The time to complete the phase", example = "P1M", accessMode = WRITE_ONLY)
	@ValidPeriod(nullable = true)
	private String timeToComplete;

	@Schema(description = "The permission needed to administrate the phase", accessMode = WRITE_ONLY)
	private Permission permission;

	@Schema(description = "The sort order of the phase", example = "1", accessMode = WRITE_ONLY)
	private Integer sortOrder;

	@Schema(description = "The id of the user updating the phase")
	@NotBlank
	private String updatedBy;
}
