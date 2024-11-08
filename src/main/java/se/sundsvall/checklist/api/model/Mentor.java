package se.sundsvall.checklist.api.model;

import jakarta.validation.constraints.NotBlank;

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
@Schema(description = "Model for a mentor on an employee checklist")
public class Mentor {

	@NotBlank
	@Schema(description = "The user-id of the mentor")
	private String userId;

	@NotBlank
	@Schema(description = "The name of the mentor")
	private String name;
}
