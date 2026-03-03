package se.sundsvall.checklist.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Paged model with summarized information for all ongoing employee checklists", accessMode = READ_ONLY)
public class OngoingEmployeeChecklists {

	@ArraySchema(schema = @Schema(implementation = OngoingEmployeeChecklist.class, accessMode = READ_ONLY))
	private List<OngoingEmployeeChecklist> checklists;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metadata;
}
