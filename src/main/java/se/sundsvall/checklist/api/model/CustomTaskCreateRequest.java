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
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for custom task create request")
public class CustomTaskCreateRequest {

	@Schema(description = "The heading of the task", example = "Bjud på fika", accessMode = WRITE_ONLY)
	@NotBlank
	private String heading;

	@Schema(description = "The body text of the task", example = "Detta är en beskrivning av ett uppdrag", accessMode = WRITE_ONLY)
	private String text;

	@Schema(description = "The question type of the task", accessMode = WRITE_ONLY)
	@NotNull
	private QuestionType questionType;

	@Schema(description = "The sort order for the task", accessMode = WRITE_ONLY)
	@NotNull
	private Integer sortOrder;

	@Schema(description = "The id of the user creating the custom task")
	@NotBlank
	private String createdBy;
}
