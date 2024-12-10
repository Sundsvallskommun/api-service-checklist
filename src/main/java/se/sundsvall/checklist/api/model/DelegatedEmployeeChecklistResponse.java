package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Model for delegated employee checklist response")
public class DelegatedEmployeeChecklistResponse {

	@ArraySchema(arraySchema = @Schema(implementation = EmployeeChecklist.class, description = "Delegated employee checklists", accessMode = READ_ONLY))
	private List<EmployeeChecklist> employeeChecklists;
}
