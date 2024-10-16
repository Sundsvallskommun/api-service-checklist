package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.integration.db.model.enums.Permission;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for task create request")
public class TaskCreateRequest {

	@Schema(description = "The name of the task", example = "Name of the task", accessMode = WRITE_ONLY)
	@NotBlank
	private String heading;

	@Schema(description = "The body text of the task", example = "Body text of the task", accessMode = WRITE_ONLY)
	private String text;

	@Schema(description = "The sort order of the task", example = "1", accessMode = WRITE_ONLY)
	@NotNull
	private Integer sortOrder;

	@Schema(description = "The role type of the task", accessMode = WRITE_ONLY)
	@NotNull
	private RoleType roleType;

	@Schema(description = "The permission needed to administrate the task", accessMode = WRITE_ONLY)
	@NotNull
	private Permission permission;

	@Schema(description = "The question type of the task", accessMode = WRITE_ONLY)
	@NotNull
	private QuestionType questionType;

}
