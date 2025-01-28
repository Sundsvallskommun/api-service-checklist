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
import se.sundsvall.checklist.integration.db.model.enums.Permission;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Task model")
public class Task {

	@Schema(description = "The id of the task", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The heading of the task", example = "Bjud på fika", accessMode = READ_ONLY)
	private String heading;

	@Schema(description = "Reference to use as value for the headings anchor element (if present)", example = "http://www.google.com", accessMode = READ_ONLY)
	private String headingReference;

	@Schema(description = "The body text of the task", example = "Detta är en beskrivning av ett uppdrag", accessMode = READ_ONLY)
	private String text;

	@Schema(description = "The sort order of the task", example = "1", accessMode = READ_ONLY)
	private int sortOrder;

	@Schema(description = "The role type eligable for the task")
	private RoleType roleType;

	@Schema(description = "The question type of the task")
	private QuestionType questionType;

	@Schema(description = "The permission needed to administrate the task")
	private Permission permission;

	@Schema(description = "The date and time the task was created", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "The date and time the task was last updated", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;

	@Schema(description = "The id of the user that last modified the task", example = "joe01doe", accessMode = READ_ONLY)
	private String lastSavedBy;
}
