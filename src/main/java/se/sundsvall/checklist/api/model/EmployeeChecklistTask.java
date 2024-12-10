package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for a employee checklist task")
public class EmployeeChecklistTask {
	@Schema(description = "The id of the task", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The heading of the task", example = "Bjud på fika", accessMode = READ_ONLY)
	private String heading;

	@Schema(description = "The body text of the task", example = "Detta är en beskrivning av ett uppdrag", accessMode = READ_ONLY)
	private String text;

	@Schema(description = "The sort order for the task", accessMode = READ_ONLY)
	private Integer sortOrder;

	@Schema(description = "The role type of the task", accessMode = READ_ONLY)
	private RoleType roleType;

	@Schema(description = "The question type of the task", accessMode = READ_ONLY)
	private QuestionType questionType;

	@Schema(description = "Tells if the task is only applies to the current checklist or not", accessMode = READ_ONLY)
	private boolean customTask;

	@Schema(description = "The task response text", example = "Jag har bjudit på fika", accessMode = READ_ONLY)
	private String responseText;

	@Schema(description = "The status of the task fulfilment", example = "TRUE", accessMode = READ_ONLY)
	private FulfilmentStatus fulfilmentStatus;

	@Schema(description = "The date and time the task was last updated", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;

	@Schema(description = "Identifier for the person that last updated the task", example = "joe01doe", accessMode = READ_ONLY)
	private String updatedBy;
}
