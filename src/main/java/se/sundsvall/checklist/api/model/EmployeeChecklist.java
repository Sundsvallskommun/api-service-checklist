package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
@Schema(description = "Model for a employee specific checklist")
public class EmployeeChecklist {

	@Schema(description = "The id of the employee checklist", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The employee connected to the checklist", accessMode = READ_ONLY)
	private Stakeholder employee;

	@Schema(description = "The manager connected to the checklist", accessMode = READ_ONLY)
	private Stakeholder manager;

	@Schema(description = "Signal if all tasks in the checklist has been completed or not", accessMode = READ_ONLY)
	private Boolean completed;

	@Schema(description = "Signal if the checklist is locked or not", accessMode = READ_ONLY)
	private boolean locked;

	@Schema(description = "Contains the email to the delegate(s) if the checklist is delegated", accessMode = READ_ONLY)
	private List<String> delegatedTo;

	@ArraySchema(arraySchema = @Schema(implementation = EmployeeChecklistPhase.class, description = "Phases in the checklist", accessMode = READ_ONLY))
	@Builder.Default
	private List<EmployeeChecklistPhase> phases = new ArrayList<>();

	@Schema(description = "The created date and time of the checklist", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "The last update date and time of the checklist", example = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;

	@Schema(description = "The id of the user that last modified the checklist")
	private String lastSavedBy;

	@Schema(description = "The date when the fulfilment of the checklist was started", example = "2023-11-22", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE)
	private LocalDate startDate;

	@Schema(description = "The date when the fulfilment of the checklist was finished", example = "2023-11-22", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE)
	private LocalDate endDate;

	@Schema(description = "The expiration date of the checklist", example = "2023-11-22", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE)
	private LocalDate expirationDate;
}
