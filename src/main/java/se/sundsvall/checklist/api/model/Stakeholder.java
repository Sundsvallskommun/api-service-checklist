package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

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
@Schema(description = "Model for a stakeholder (employee or manager) to an employee checklist")
public class Stakeholder {

	@Schema(description = "The person id for the stakeholder", examples = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The first name for the stakeholder", examples = "John", accessMode = READ_ONLY)
	private String firstName;

	@Schema(description = "The last name for the stakeholder", examples = "Doe", accessMode = READ_ONLY)
	private String lastName;

	@Schema(description = "The email address for the stakeholder", examples = "email.address@noreply.com", accessMode = READ_ONLY)
	private String email;

	@Schema(description = "The username for the stakeholder", examples = "abc12def", accessMode = READ_ONLY)
	private String username;

	@Schema(description = "The job title for the stakeholder (if applicable)", examples = "Skoladministratör (Sundsvalls kommun)", accessMode = READ_ONLY)
	private String title;
}
