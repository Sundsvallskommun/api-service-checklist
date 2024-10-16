package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for an organizational unit")
public class Organization {

	@Schema(description = "The id of the unit", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The name of the unit", example = "Sundsvall Energi", accessMode = READ_ONLY)
	private String organizationName;

	@Schema(description = "The organization number of the unit", example = "5345", accessMode = READ_ONLY)
	private int organizationNumber;

	@ArraySchema(schema = @Schema(description = "All checklists connected to the organization", accessMode = READ_ONLY))
	private List<Checklist> checklists;

	@ArraySchema(schema = @Schema(description = "Valid channels to use when communicating with the organization", accessMode = READ_ONLY))
	private Set<CommunicationChannel> communicationChannels;

	@Schema(description = "The date and time the unit was created", example = "2023-11-22T15:30:00+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "The date and time the unit was updated", example = "2023-11-22T15:30:00+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime updated;
}
