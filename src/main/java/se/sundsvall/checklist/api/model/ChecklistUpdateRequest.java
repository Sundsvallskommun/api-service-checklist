package se.sundsvall.checklist.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for checklist update request")
public class ChecklistUpdateRequest {

	@Schema(description = "The name of the checklist", example = "New display name")
	private String displayName;

	@Schema(description = "The role type of the checklist")
	private RoleType roleType;

}
