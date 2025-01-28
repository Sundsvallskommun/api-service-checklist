package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Model for custom task update request")
public class CustomTaskUpdateRequest {

	@Schema(description = "The heading of the task", example = "Bjud på fika", accessMode = WRITE_ONLY)
	private String heading;

	@Schema(description = "Optional reference to use as value for the headings anchor element", example = "http://www.google.com", accessMode = WRITE_ONLY)
	private String headingReference;

	@Schema(description = "The body text of the task", example = "Detta är en beskrivning av ett uppdrag", accessMode = WRITE_ONLY)
	private String text;

	@Schema(description = "The question type of the task")
	private QuestionType questionType;

	@Schema(description = "The sort order for the task", accessMode = WRITE_ONLY)
	private Integer sortOrder;

	@Schema(description = "The id of the user updating the custom task", example = "joe01doe", accessMode = WRITE_ONLY)
	@NotBlank
	private String updatedBy;
}
