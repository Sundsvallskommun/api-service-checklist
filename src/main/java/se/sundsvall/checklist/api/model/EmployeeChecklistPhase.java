package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for a employee checklist phase")
public class EmployeeChecklistPhase {

	@Schema(description = "The id of the phase", examples = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The name of the phase", examples = "Första veckan", accessMode = READ_ONLY)
	private String name;

	@Schema(description = "The body text of the phase", examples = "Detta är en beskrivning av vad som ska göras under första veckan", accessMode = READ_ONLY)
	private String bodyText;

	@Schema(description = "The time to complete the phase", examples = "P1M", accessMode = READ_ONLY)
	private String timeToComplete;

	@Schema(description = "The sort order for the phase", examples = "1", accessMode = READ_ONLY)
	private int sortOrder;

	@ArraySchema(arraySchema = @Schema(implementation = EmployeeChecklistTask.class, description = "Tasks in the phase", accessMode = READ_ONLY))
	@Builder.Default
	private List<EmployeeChecklistTask> tasks = new ArrayList<>();

}
