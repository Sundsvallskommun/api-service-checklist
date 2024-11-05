package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Schema(description = "Model for checklist")
public class Checklist {

	@Schema(description = "The id of the checklist", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The role type of the checklist", accessMode = READ_ONLY)
	private RoleType roleType;

	@Schema(description = "The name of the checklist", example = "Checklist_A", accessMode = READ_ONLY)
	private String name;

	@Schema(description = "The display name of the checklist", example = "Display name", accessMode = READ_ONLY)
	private String displayName;

	@Schema(description = "The version of the checklist", example = "1", accessMode = READ_ONLY)
	private Integer version;

	@Schema(description = "The lifecycle of the checklist", accessMode = READ_ONLY)
	private LifeCycle lifeCycle;

	@Schema(description = "The created date and time of the checklist", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "The last update date and time of the checklist", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;

	@Schema(description = "The id of the user that last modified the checklist")
	private String lastSavedBy;

	@ArraySchema(arraySchema = @Schema(implementation = Task.class, description = "Phases in the checklist", accessMode = READ_ONLY))
	private List<Phase> phases;
}
