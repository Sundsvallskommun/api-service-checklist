package se.sundsvall.checklist.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for checklist update request")
public class ChecklistUpdateRequest {

	@Schema(description = "The name of the checklist", example = "New display name")
	private String displayName;

	@Schema(description = "The id of the user updating the checklist")
	@NotBlank
	private String updatedBy;
}
