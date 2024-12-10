package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.checklist.api.validation.ValidChannels;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for organizational unit update request")
public class OrganizationUpdateRequest {

	@Schema(description = "The name of the unit", example = "Sundsvall Energi", accessMode = WRITE_ONLY, nullable = true)
	private String organizationName;

	@ArraySchema(schema = @Schema(description = "Valid channels to use when communicating with the organization", accessMode = WRITE_ONLY), uniqueItems = true)
	@ValidChannels(nullable = true)
	private Set<CommunicationChannel> communicationChannels;
}
