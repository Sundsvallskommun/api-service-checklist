package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.integration.db.model.enums.Permission;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for a phase")
public class Phase {

	@Schema(description = "The id of the phase", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The name of the phase", example = "Första veckan", accessMode = READ_ONLY)
	private String name;

	@Schema(description = "The body text of the phase", example = "Detta är en beskrivning av vad som ska göras under första veckan", accessMode = READ_ONLY)
	private String bodyText;

	@Schema(description = "The time to complete the phase", example = "P1M", accessMode = READ_ONLY)
	private String timeToComplete;

	@Schema(description = "The role type of the phase", accessMode = READ_ONLY)
	private RoleType roleType;

	@Schema(description = "The permission needed to administrate the phase", accessMode = READ_ONLY)
	private Permission permission;

	@Schema(description = "The sort order of the phase", example = "1", accessMode = READ_ONLY)
	private int sortOrder;

	@ArraySchema(arraySchema = @Schema(implementation = Task.class, description = "Tasks in the phase", accessMode = READ_ONLY))
	private List<Task> tasks;

	@Schema(description = "The created date and time of the phase", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "The last update date and time of the phase", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;

}
