package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import jakarta.validation.constraints.NotBlank;

import se.sundsvall.checklist.integration.db.model.enums.QuestionType;

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
@Schema(description = "Model for custom task update request")
public class CustomTaskUpdateRequest {

	@Schema(description = "The heading of the task", example = "Bjud på fika")
	private String heading;

	@Schema(description = "The body text of the task", example = "Detta är en beskrivning av ett uppdrag")
	private String text;

	@Schema(description = "The question type of the task")
	private QuestionType questionType;

	@Schema(description = "The sort order for the task", accessMode = WRITE_ONLY)
	private Integer sortOrder;

	@Schema(description = "The id of the user updating the custom task")
	@NotBlank
	private String updatedBy;
}
