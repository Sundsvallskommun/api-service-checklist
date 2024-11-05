package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Model for organizational unit create request")
public class OrganizationCreateRequest {

	@Schema(description = "The name of the unit", example = "Sundsvall Energi", accessMode = WRITE_ONLY)
	@NotBlank
	private String organizationName;

	@Schema(description = "The organization number of the unit", example = "5345", accessMode = WRITE_ONLY)
	@NotNull
	private Integer organizationNumber;

	@ArraySchema(minContains = 1, schema = @Schema(description = "Valid channels to use when communicating with the organization", accessMode = WRITE_ONLY))
	@ValidChannels
	private Set<CommunicationChannel> communicationChannels;

}
