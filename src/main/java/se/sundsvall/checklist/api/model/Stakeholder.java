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

	@Schema(description = "The person id for the stakeholder", example = "5a6c3e4e-c320-4006-b448-1fd4121df828", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The first name for the stakeholder", example = "John", accessMode = READ_ONLY)
	private String firstName;

	@Schema(description = "The last name for the stakeholder", example = "Doe", accessMode = READ_ONLY)
	private String lastName;

	@Schema(description = "The email address for the stakeholder", example = "email.address@noreply.com", accessMode = READ_ONLY)
	private String email;

	@Schema(description = "The user name for the stakeholder", example = "abc12def", accessMode = READ_ONLY)
	private String userName;
}
