package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for information about the last execution to initiate employee checklists")
public class InitiationInformation {

	@Schema(description = "The log id for the execution (used for investigation purpose when searching logs in ELK)", examples = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String logId;

	@Schema(description = "A information summary for the execution", examples = "4 potential problems occurred when importing 6 employees", accessMode = READ_ONLY)
	private String summary;

	@Schema(description = "The execution date and time for the initiation", examples = "2023-11-22T15:30:00+03:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime executed;

	@Schema(description = "A list with detailed information for each employee checklist initiation", accessMode = READ_ONLY)
	private List<Detail> details;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Builder(setterPrefix = "with")
	@Schema(description = "Model for detailed information for a specific employee checklist initiation")
	public static class Detail {

		@Schema(description = "Status for the employee checklist initiation", examples = "200", accessMode = READ_ONLY)
		private int status;

		@Schema(description = "Information regarding the employee checklist initiation", examples = "Employee with loginname pau55rod processed successfully.", accessMode = READ_ONLY)
		private String information;
	}
}
