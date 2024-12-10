package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for custom sort order request")
public class SortorderRequest {

	@Schema(description = "List containing sort order for phase items", accessMode = WRITE_ONLY)
	private List<@Valid PhaseItem> phaseOrder;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@SuperBuilder(setterPrefix = "with")
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	@Schema(description = "Model for a phase item in the sort order structure")
	public static class PhaseItem extends TaskItem {

		@Schema(description = "List containing sort order for the task items connected to the phase", accessMode = WRITE_ONLY)
		private List<@Valid TaskItem> taskOrder;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@SuperBuilder(setterPrefix = "with")
	@Schema(description = "Model for a task item in the sort order structure")
	public static class TaskItem {

		@Schema(description = "The id for the item", example = "283cec0f-b6eb-473c-9dbb-d97a959a8144", accessMode = WRITE_ONLY)
		@ValidUuid
		private String id;

		@Schema(description = "The sort order position for the item", example = "1", accessMode = WRITE_ONLY)
		@NotNull
		private Integer position;
	}
}
